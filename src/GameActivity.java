import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GameActivity extends Application {
    //should change that to 1200 and 1000 and then adjust radius
    final public static int defaultPadding = 50;
    final public static int canvasHeight = 750;
    final public static int canvasWidth = 500;
    final public static String[] colors = {"BLUE", "RED","GREEN", "YELLOW"};
    final public static Color[] colorsC = {Color.blue,Color.red,Color.green,Color.yellow};
    final public static Color neutralColor = Color.black;
    final public static String countdownString = "Countdown: ";
    final public static int countdownLength = 3;
    final public static double defaultSpeed = 1;
    final public static double defaultRadius = 13;
    final public static float defaultStokeWidth = 4;
    final public static double defaultDirChangeSpeed = 0.03;
    final public static long refreshTime = 1000/120;
    final public static int LEFT = 1;
    final public static int STRAIGHT = 2;
    final public static int RIGHT = 3;

    public Socket[] clients;
    public BufferedReader[] readers;
    public BufferedWriter[] writers;
    public int amountPlayer;
    public int playerAlive;
    public int[] placements;

    public VBox root;
    public ImageView imageView;
    public WritableImage writableImage;
    public BufferedImage bufferedImage;
    public Graphics2D graph;
    public Ellipse2D[] circleView;
    public GeneralPath[] path;

    public double[] circlePositionsX;
    public double[] circlePositionsY;
    public double[] circleDirections;
    public double[] circleSpeeds;

    public int[] dirChanges;
    public double[] dirChangeSpeed;
    public double[] widthAndHeight;
    public double[] radius;


    public GameActivity(Socket[] clients, BufferedReader[] readers, BufferedWriter[] writers){
            this.clients = clients;
            this.readers = readers;
            this.writers = writers;
            amountPlayer = clients.length;
            playerAlive = amountPlayer;
            circlePositionsX = new double[amountPlayer];
            circlePositionsY = new double[amountPlayer];
            circleDirections = new double[amountPlayer];
            dirChangeSpeed = new double[amountPlayer];
            circleSpeeds = new double[amountPlayer];
            dirChanges = new int[amountPlayer];
            placements = new int[amountPlayer];
            widthAndHeight = new double[amountPlayer];
            radius = new double[amountPlayer];
            initCanvas();
            initCircles();
    }
    //this should be deleted because it should be called by the Constructor
    public void startNormalGame(Socket[] clients, BufferedReader[] readers, BufferedWriter[] writers){
        this.clients = clients;
        this.readers = readers;
        this.writers = writers;
        amountPlayer = clients.length;
        playerAlive = amountPlayer;
        circlePositionsX = new double[amountPlayer];
        circlePositionsY = new double[amountPlayer];
        circleDirections = new double[amountPlayer];
        dirChangeSpeed = new double[amountPlayer];
        circleSpeeds = new double[amountPlayer];
        dirChanges = new int[amountPlayer];
        placements = new int[amountPlayer];
        widthAndHeight = new double[amountPlayer];
        radius = new double[amountPlayer];
        initCanvas();
        initCircles();
    }
    //should get deleted but needed for Application
    public GameActivity(){
        super();
    }

    private void initCanvas() {
        this.bufferedImage = new BufferedImage(GameActivity.canvasWidth, GameActivity.canvasHeight,BufferedImage.TYPE_INT_RGB);
        this.graph = this.bufferedImage.createGraphics();
        this.graph.setStroke(new BasicStroke(defaultStokeWidth));
        this.writableImage = SwingFXUtils.toFXImage(bufferedImage,null);
        imageView = new ImageView(writableImage);
        circleView = new Ellipse2D[amountPlayer];
        path = new GeneralPath[amountPlayer];
    }

    private void initCircles(){
        Random r = new Random();
        for(int i = 0;i<amountPlayer;i++){
            try {
                clients[i].setSoTimeout(0);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            circlePositionsX[i] = r.nextDouble() * (bufferedImage.getWidth() - 2*defaultPadding) + defaultPadding;
            circlePositionsY[i] = r.nextDouble() * (bufferedImage.getHeight() - 2* defaultPadding) + defaultPadding;
            circleDirections[i] = r.nextDouble() * 2*Math.PI;
            circleSpeeds[i] = GameActivity.defaultSpeed;
            dirChangeSpeed[i] = GameActivity.defaultDirChangeSpeed;
            widthAndHeight[i] = GameActivity.defaultRadius;
            radius[i] = widthAndHeight[i] / 2;
            dirChanges[i] = GameActivity.STRAIGHT;
            placements[i] = 1;


            path[i] = new GeneralPath();
            path[i].moveTo(circlePositionsX[i]+ radius[i],circlePositionsY[i]+ radius[i]);
            circleView[i] = new Ellipse2D.Double(circlePositionsX[i],circlePositionsY[i],widthAndHeight[i],widthAndHeight[i]);
            this.graph.fill(circleView[i]);
        }
        circleDirections[0] = 0;

    }

    public void startGame(){
        //here comes all code of start Method of fx in once its finished
    }

    private void sendCirclePositions() {
        for(int i= 0; i<amountPlayer; i++){
            try {
                for(int color = 0; color<amountPlayer;color++){
                    writers[i].write(GameActivity.colors[color] + ";" + (circlePositionsX[color]/canvasWidth) + ";" + (circlePositionsY[color]/canvasHeight) + "\n");
                }
                writers[i].flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void actPositions() {
        for(int i = 0; i<amountPlayer; i++){
            //first direction
            switch (dirChanges[i]){
                case GameActivity.LEFT:
                    circleDirections[i] += dirChangeSpeed[i];
                    break;
                case GameActivity.RIGHT:
                    circleDirections[i] -= dirChangeSpeed[i];
                    break;
            }
            //then position

            circlePositionsX[i] = circlePositionsX[i] + Math.cos(circleDirections[i]) * circleSpeeds[i];
            circlePositionsY[i] = circlePositionsY[i] - Math.sin(circleDirections[i]) * circleSpeeds[i];

            this.graph.setPaint(neutralColor);
            this.graph.fill(circleView[i]);

            if(hitsSomething(i)){
                endCircle(i);
            }

            this.graph.setPaint(colorsC[i]);
            this.circleView[i].setFrame(circlePositionsX[i],circlePositionsY[i], widthAndHeight[i], widthAndHeight[i]);
            this.path[i].lineTo(circlePositionsX[i] + radius[i],circlePositionsY[i] + radius[i]);

            this.graph.fill(circleView[i]);
            this.graph.draw(path[i]);


        }

        writableImage = SwingFXUtils.toFXImage(bufferedImage,null);
        imageView.setImage(writableImage);
    }

    private boolean hitsSomething(int index) {
        if (circlePositionsX[index] <= 0 || circlePositionsX[index] >= (bufferedImage.getWidth() - widthAndHeight[index]) || circlePositionsY[index] >= (bufferedImage.getHeight() - widthAndHeight[index]) || circlePositionsY[index] <= 0) {
            return true;
        }
        double anzPixel = 11;
        int pixel;
        double dir;
        for(int i = 0; i<anzPixel; i++){
            dir = circleDirections[index] + ((i - (anzPixel/2)) / (anzPixel/2)) * 0.5 * Math.PI;
            pixel = bufferedImage.getRGB((int)(circlePositionsX[index] + radius[index] + (int) (Math.cos(dir) * radius[index])), (int) (circlePositionsY[index] + radius[index] - (int) (Math.sin(dir) * radius[index])));
            if(pixel != 0 && pixel != GameActivity.neutralColor.getRGB()){
                return true;
            }
        }
        return false;
    }

    private void endCircle(int index){
        circleSpeeds[index] = 0;
        placements[index] = playerAlive;
        playerAlive--;
    }

    private void sendStartCountdown(){
        int actCountdown = GameActivity.countdownLength;
        while(actCountdown>0){
            for(BufferedWriter br:writers){
                try {
                    br.write(countdownString + actCountdown + "\n");
                    for(int color = 0; color<amountPlayer;color++){
                        br.write(GameActivity.colors[color] + ";" + (circlePositionsX[color]/ GameActivity.canvasWidth) + ";" + (circlePositionsY[color]/ GameActivity.canvasHeight) + ";" + circleDirections[color] + "\n");
                    }
                    br.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            actCountdown--;
        }
    }

    private void endGame(){
        for(int i = 0; i<amountPlayer; i++){
            try {
                writers[i].write("Placement: " + placements[i] + "\n");
                writers[i].flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ClientHandler.closeClient(clients[i]);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        startNormalGame(SocketHandler.socket, SocketHandler.br,SocketHandler.bw);

        for(int i = 0; i<amountPlayer;i++){
//            new ListenToClients(readers[i],this,i).start();
        }

        Thread thread = new Thread(() -> {
            sendStartCountdown();
            while(playerAlive > 1){

                actPositions();
                sendCirclePositions();

                try {
                    Thread.sleep(GameActivity.refreshTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            endGame();
        });
        thread.start();

        root = new VBox();
        root.getChildren().add(imageView);
        primaryStage.setScene(new Scene(root, GameActivity.canvasWidth, GameActivity.canvasHeight));
        primaryStage.setTitle("Canvas");
        primaryStage.show();
    }


}
