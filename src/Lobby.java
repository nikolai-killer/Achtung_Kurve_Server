import javafx.application.Application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class Lobby {
    public static final int maxPlayer = 4;

    private LinkedList<Player> player;
    private LinkedList<String> playerNames;
    private Player partyOwner;

    public Lobby(Player player) {
        this.player = new LinkedList<>();
        playerNames = new LinkedList<>();
        setPartyOwner(player);
        this.player.add(player);
        this.playerNames.add(player.getName());
    }

    public Player getPartyOwner(){
        return this.partyOwner;
    }

    public void setPartyOwner(Player player) {
        this.partyOwner = player;
    }

    public int getNumberOfPlayer(){
        return this.player.size();
    }

    public String[] getAllPlayerNames(){
        return playerNames.toArray(new String[0]);
    }

    public void addPlayer(Player player) {
        if(this.player.size() < Lobby.maxPlayer){
            this.player.add(player);
            this.playerNames.add(player.getName());
            actLobbyListAllPlayer();
        }
    }

    public void setLobby(Lobby lobby){
        this.player = lobby.player;
        this.partyOwner = lobby.partyOwner;
    }

    public boolean contains(Player player) {
        return this.player.contains(player);
    }

    public void deletePlayer(Player player){
        if(this.player.contains(player)){
            if(this.player.indexOf(player) == 0){
                if(this.player.size()>1){
                    this.setPartyOwner(this.player.get(1));
                }
            }
            this.player.remove(player);
            this.playerNames.remove(player.getName());
            actLobbyListAllPlayer();
        }
    }

    public void startGame(){
        Socket[] clients = new Socket[player.size()];
        BufferedReader[] readers = new BufferedReader[player.size()];
        BufferedWriter[] writers = new BufferedWriter[player.size()];
        for(int i = 0;i<player.size();i++){
            Player actPlayer = player.get(i);
            clients[i] = actPlayer.getClientHandler().getClientSocket();
            readers[i] = actPlayer.getClientHandler().getClientReader();
            writers[i] = actPlayer.getClientHandler().getClientWriter();
            actPlayer.firePropChangeGameStart();
        }
        for(int i = 0; i<ClientHandler.ignoreString.length()+1;i++){
            try {
               partyOwner.getClientHandler().getClientReader().read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Server.allPlayer.remove(this.getPartyOwner());
        Game game = new Game(clients,readers,writers);
        game.startGame();
//        SocketHandler.socket = clients;
//        SocketHandler.br = readers;
//        SocketHandler.bw = writers;
//        Application.launch(GameActivity.class,"");

    }

    private void actLobbyListAllPlayer(){
        for(Player i : this.player){
            i.firePropChangeLobbyAct();
        }
    }
}
