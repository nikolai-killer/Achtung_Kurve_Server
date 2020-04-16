import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    public static ServerSocket serverSocket;
    public static int serverPort = 6969;

    public static LinkedList<Player> allPlayer;

    public static void main(String[] args) {
        allPlayer = new LinkedList<>();

        try {
            DatagramSocket getIpSocket = new DatagramSocket();
            getIpSocket.connect(InetAddress.getByName("8.8.8.8") ,10002);
            System.out.println(getIpSocket.getLocalAddress().getHostAddress());
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while(true) {
            try {
                System.out.println("Waiting for accept");
                Socket client = serverSocket.accept();
                new ClientHandler(client).start();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}