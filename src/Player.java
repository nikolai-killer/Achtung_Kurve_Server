import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Player {
    private String name;
    private Lobby lobby;
    private ClientHandler clientHandler;
    public Lobby lobbyInvite;
    public boolean isInGame;

    protected PropertyChangeSupport propertyChangeSupport;

    public Player(String name, ClientHandler clientHandler){
        this.clientHandler = clientHandler;
        this.name = name;
        this.isInGame = false;
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void initLobby(){
        lobby = new Lobby(this);
        lobbyInvite = new Lobby(this);
    }

    public Lobby getLobby(){
        if(this.lobby == null){
            return null;
        }
        return this.lobby;
    }

    public String getName(){
        return name;
    }

    public ClientHandler getClientHandler(){
        return this.clientHandler;
    }

    public void sendInviteTo(String message){
        String player = message.substring(ClientHandler.inviteString.length(),message.length()-1);
        for(Player i : Server.allPlayer){
            if(i.getName().equals(player)){
                i.lobbyInvite.setLobby(this.getLobby());
                i.firePropChangeInvite(this.getName());
            }
        }
    }

    public void acceptInvite(String message){
        Player inviter = null;
        String playerName = message.substring(ClientHandler.acceptInv.length(),message.length()-1);
        for(Player i : Server.allPlayer){
            if(i.getName().equals(playerName)){
                inviter = i;
            }
        }
        if(inviter != null && inviter.getLobby().getNumberOfPlayer()<Lobby.maxPlayer){
            this.lobby = inviter.getLobby();
            this.lobby.addPlayer(this);
        }
        else{
            clientHandler.sendErrorNotification("Lobby full or player left the game");
        }
    }

    public void quitLobby(){
        Lobby temp = getLobby();
        initLobby();
        temp.deletePlayer(this);
        clientHandler.sendLobby();
    }

    public void tryStartGame(){
        if(getLobby().getNumberOfPlayer()<2){
            clientHandler.sendErrorNotification("At least two Player needed");
        }
        else if(!getLobby().getPartyOwner().equals(this)){
            clientHandler.sendErrorNotification("You are not the Party Owner");
        }
        else{
            Thread t = new Thread(() -> {
                getLobby().startGame();

            });
            t.start();
            this.getClientHandler().notInGame = false;
        }
    }

    public void die(){
       getLobby().deletePlayer(this);
    }



    public void addPropertyChangeListener(PropertyChangeListener listener){
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    public void firePropChangeInvite(String name) {
        propertyChangeSupport.firePropertyChange(ClientHandler.NEWINVITE,"old",name);
    }
    public void firePropChangeLobbyAct(){
        propertyChangeSupport.firePropertyChange(ClientHandler.LOBBYACT,"old","new");
    }
    public void firePropChangeGameStart(){
        propertyChangeSupport.firePropertyChange(ClientHandler.GOGAME,"old","new");
    }
}
