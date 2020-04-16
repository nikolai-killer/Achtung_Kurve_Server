import java.io.BufferedReader;
import java.io.IOException;

public class ListenToClients extends Thread{

    private BufferedReader bufferedReader;
    private Game gameActivity;
    private int index;

    public ListenToClients(BufferedReader bufferedReader, Game gameActivity, int index){
        this.bufferedReader = bufferedReader;
        this.gameActivity = gameActivity;
        this.index = index;
    }

    @Override
    public void run() {
        super.run();
        int dir = 0;
        boolean streamOpen = true;
        while(gameActivity.amountPlayer>1 && streamOpen && dir != -1){
            try {
                dir = bufferedReader.read();
                gameActivity.dirChanges[index] = dir;
            } catch (IOException e) {
                e.printStackTrace();
                streamOpen = false;
            }
        }
    }
}
