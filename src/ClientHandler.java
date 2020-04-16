import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread implements PropertyChangeListener {

    private final static int timeout = 5000;

    //to read
    public final static String heartbeatString = "Heartbeat: ";
    public final static String inviteString = "Invite\n";
    public final static String acceptInv = "AcceptInv\n";
    public final static String quitLobbyString = "QuitLobby\n";
    public final static String ignoreString = "Ignore\n";
    private final static int HEARTBEATCODE = 1;
    private final static int INVITECODE = 2;
    private final static int ACCEPTINVCODE = 3;
    private final static int QUITLOBBYCODE = 4;
    private final static int GOGAMECODE = 5;
    private final static int IGNORE = 6;

    //to write
    public final static String goGameHeader = "GoGame ";
    private final static String playerListHeader = "AllPlayer";
    public final static String lobbyListHeader = "Lobby\n";
    public final static String errorNotificationHeader = "Error\n";
    public final static String NEWINVITE = "newInviteText";
    public static final String LOBBYACT = "acceptedInvText";
    public final static String GOGAME = "goGameText";

    private Socket client;
    private BufferedReader clientReader;
    private BufferedWriter clientWriter;
    public Player player;
    public boolean notInGame = true;

    public ClientHandler(Socket client){
        this.client = client;
    }

    public Socket getClientSocket(){
        return client;
    }
    public BufferedReader getClientReader(){
        return this.clientReader;
    }
    public BufferedWriter getClientWriter(){
        return this.clientWriter;
    }

    @Override
    public void run() {
        try {
            clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            clientWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            client.setSoTimeout(ClientHandler.timeout);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            String line;
            StringBuilder message;
            while(notInGame){
                message = new StringBuilder();
                while (!(line = clientReader.readLine()).equals("")) {
                    message.append(line).append("\n");
                }
                handleMessage(message.toString());
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }


        if(notInGame){
            this.player.die();
            Server.allPlayer.remove(this.player);
            ClientHandler.closeClient(this.client);
        }
        else{
            Server.allPlayer.remove(this.player);
        }
    }

    private void handleMessage(String message) {
        switch(getMessageCode(message)){
            case HEARTBEATCODE:
                if(this.player == null){
                    this.player = new Player(message.substring(heartbeatString.length(),message.length()-1),this);
                    this.player.initLobby();
                    this.player.addPropertyChangeListener(this);
                    if(!Server.allPlayer.contains(this.player)){
                        Server.allPlayer.add(this.player);
                    }
                }
                sendPlayerList();
                break;
            case INVITECODE:
                if(this.player != null && this.player.getLobby() != null){
                    this.player.sendInviteTo(message);
                }
                break;
            case ACCEPTINVCODE:
                if(this.player != null && this.player.lobbyInvite != null){
                    this.player.acceptInvite(message);
                }
                break;
            case QUITLOBBYCODE:
                this.player.quitLobby();
                break;
            case GOGAMECODE:
                this.player.tryStartGame();
                break;
            case IGNORE:
                System.out.println(message);
                break;
        }
    }

    private int getMessageCode(String message) {
        if(message.startsWith(heartbeatString)){
            return HEARTBEATCODE;
        }
        else if(message.startsWith(inviteString)){
            return INVITECODE;
        }
        else if(message.startsWith(acceptInv)){
            return ACCEPTINVCODE;
        }
        else if(message.startsWith(quitLobbyString)){
            return QUITLOBBYCODE;
        }
        else if(message.startsWith(goGameHeader)){
            return GOGAMECODE;
        }
        else if(message.startsWith(ignoreString)){
            return IGNORE;
        }

        return -1;
    }

    public void sendErrorNotification(String message){
        try {
            clientWriter.write(ClientHandler.errorNotificationHeader + message + "\n\n");
            clientWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPlayerList() {
        StringBuilder message = new StringBuilder(ClientHandler.playerListHeader + "\n");
        for(Player i : Server.allPlayer){
            message.append(i.getName()).append("\n");
        }
        message.append("\n");

        synchronized (this){
            try {
                clientWriter.write(message.toString());
                clientWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendLobby(){
        StringBuilder message = new StringBuilder(ClientHandler.lobbyListHeader);
        String[] allPlayer = this.player.getLobby().getAllPlayerNames();
        for (String s : allPlayer) {
            message.append(s).append("\n");
        }
        message.append("\n");
        synchronized (this){
            try {
                clientWriter.write(message.toString());
                clientWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()){
            case ClientHandler.NEWINVITE:
                try {
                    clientWriter.write(ClientHandler.inviteString + evt.getNewValue() + "\n\n");
                    clientWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ClientHandler.LOBBYACT:
                this.sendLobby();
                this.sendPlayerList();
                break;
            case ClientHandler.GOGAME:
                this.notInGame = false;
                try {
                    clientWriter.write(ClientHandler.goGameHeader + this.player.getLobby().getNumberOfPlayer()  + "\n\n");
                    clientWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public static void closeClient(Socket socket){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



