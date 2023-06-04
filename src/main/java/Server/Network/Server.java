package Server.Network;

import Server.Controller.Controller;
import Server.Events.SelectViewEvents.LoginView;
import Server.Events.SelectViewEvents.SelectViewEvent;
import Server.Model.Match;
import Server.Model.MatchStatus.NotRunning;
import Server.Model.MatchStatus.WaitingForPlayers;
import Utils.ConnectionInfo;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;


/**
 * This class is the main class of the Server and is used to istantiate the RMI waiter and the Socket waiter,
 * which will wait for clients to connect to the server.
 * @see Server.Network.RMIWaiter
 * @see Server.Network.SocketWaiter
 * @author patrickpoggi
 */
public class Server implements Runnable{
    SocketWaiter socketWaiter;
    RMIWaiterInterface rmiWaiter;
    Registry rmiRegistry;
    List<Match> matches;
    Map<Match, Controller> macthesControllers;
    Map<Match, List<VirtualView>> matchesViews;
    PingManager pingManager;
    Map<ConnectionInfo, Boolean> clientsConnectionStatuses;

    Queue<VirtualView> clientsWaitingForMatch;

    public Server() throws IOException{
        boolean done = false;
        this.matches = new ArrayList<>();
        matchesViews = new HashMap<>();
        this.macthesControllers = new HashMap<>();
        this.clientsConnectionStatuses = new HashMap<>();
        pingManager = new PingManager(this, new ArrayList<>(), new HashMap<>());
        this.socketWaiter = new SocketWaiter(this,1098);
        this.clientsWaitingForMatch = new LinkedList<>();
        while(!done){
            try{
                this.rmiWaiter = new RMIWaiter(this);
                done = true;
            }catch (RemoteException e){
                System.err.println("Impossible to istantiate the RMI waiter");
                System.err.println(e.getStackTrace());
            }
        }

    }

    public static void main(String[] args) {
        try{
            Server server = new Server();
            server.run();
        }catch (IOException e){
            System.err.println("Error occurred when trying to istantiate the socket waiter");
            System.err.println(e.getStackTrace());
        }
    }


    /**
     * This method is used to start the server. It will bind the RMI waiter to the RMI registry and start the socket
     * waiter in a new thread
     */
    @Override
    public void run() {
        boolean done = false;
        new Thread(socketWaiter).start();
        done = false;
        while(!done){
            try{
                this.rmiRegistry = LocateRegistry.createRegistry(1099);
                System.setProperty("java.rmi.server.hostname","localhost");
                done = true;
            }catch (RemoteException e){
                System.err.println(e.getStackTrace());
            }
        }

        done = false;
        while(!done){
            try{
                rmiRegistry.bind("RMIWaiter", rmiWaiter);
                done = true;
            }catch (AccessException e){
                System.err.println(e.getStackTrace());
            }catch (RemoteException e){
                System.err.println(e.getStackTrace());
            }catch (AlreadyBoundException e){
                System.err.println(e.getStackTrace());
            }
        }
        new Thread(pingManager).start();
        System.out.println("Server started");
    }

    /**
     * This method is used from both the RMI waiter and the Socket waiter to see if there is a match which is waiting
     * for players to join.
     * @return true if and only if there is a match which is waiting for players to join. If that's the case it must be
     * the last match in the list of matches.
     */
    protected synchronized boolean waitingMatch(){
        if(matches.size() == 0)
            return false;
        //return (matches.get(matches.size()-1).getMatchStatus() instanceof WaitingForPlayers) || (matches.get(matches.size()-1).getMatchStatus() instanceof NotRunning) ;
        return (matches.get(matches.size()-1).getMatchStatus() instanceof WaitingForPlayers);
    }

    public synchronized boolean matchWaitingForInit(){
        if(matches.size()==0){
            return false;
        }
        return (matches.get(matches.size()-1).getMatchStatus() instanceof NotRunning);
    }

    public synchronized Match getMatchWaitingForInit(){
        return matches.get(matches.size()-1);
    }

    /**
     * This method is used from both the RMI waiter and the Socket waiter to get the match which is waiting for players
     * @return the match which is waiting for players.
     */
    protected synchronized Match getWaitingMatch(){
        return matches.get(matches.size()-1);
    }

    /**
     * This method is used from both the RMI waiter and the Socket waiter to get the controller of a given
     * @return the controller of the given match
     */
    protected  Controller getMatchsController(Match m){
        synchronized (macthesControllers){
            return macthesControllers.get(m);
        }
    }

    /**
     * This method is used from both the RMI waiter and the Socket waiter to tell the server that a new match has been
     * created and that it must be added to the list of matches.
     * @param m is the new match
     * @param c is the controller of the new match
     * @param vv is the first virtual view we have istantiated for the new match, since if we had to create a new match
     *           there must have been one client connected to the server.
     */
    protected synchronized void subscribeNewMatch(Match m, Controller c, VirtualView vv){
        matches.add(m);
        macthesControllers.put(m,c);
        matchesViews.put(m, new ArrayList<>());
        matchesViews.get(m).add(vv);
        pingManager.addVirtualView(vv, c);
    }

    protected synchronized void subscribeNewViewToExistingMatch(Match m, VirtualView vv){
        //matchesViews.put(m,vv);
        matchesViews.get(m).add(vv);
        pingManager.addVirtualView(vv, macthesControllers.get(m));
    }

    public synchronized void subscribeNewWaitingClient(VirtualView waitingClient){
        this.clientsWaitingForMatch.add(waitingClient);
    }

    public Queue<VirtualView> dequeueWaitingClients(){
        Match lastMatch = matches.get(matches.size()-1);
        synchronized (matches){
            lastMatch = matches.get(matches.size()-1);
        }
        Queue<VirtualView> noMoreWaitingClients = new LinkedList<>();
        if(lastMatch.getMatchStatus() instanceof WaitingForPlayers){
            int numbeOfMissingPlayers = lastMatch.getNumberOfPlayers()-lastMatch.getPlayers().size();
            while(numbeOfMissingPlayers>0){
                VirtualView vv;
                synchronized (clientsWaitingForMatch){
                    vv = clientsWaitingForMatch.poll();
                }
                if(vv == null){
                    System.err.println("Server.evolveLastMatch(): There are no more clients waiting for a match");
                    return noMoreWaitingClients;
                }
                noMoreWaitingClients.add(vv);
                lastMatch.addMVEventListener(vv);
                this.subscribeNewViewToExistingMatch(lastMatch, vv);
                this.updateConnectionStatus(vv.getConnectionInfo(), true);
                numbeOfMissingPlayers--;
            }
            //If there are still clients waiting for joining a match, the first one will be a match opener and the others
            //wil continue waiting
            if(!clientsWaitingForMatch.isEmpty()){
                VirtualView vv;
                synchronized (clientsWaitingForMatch){
                    vv = clientsWaitingForMatch.poll();
                }
                if(vv == null){
                    throw new RuntimeException("[Server.dequeueWaitingClients()]: " +
                            "There seem to be clients waiting for a match, but the queue is empty");
                }
                vv.setIsFirstToJoin(true);
                Match m = new Match();
                Controller c = new Controller(m, this);
                m.addMVEventListener(vv);
                vv.addVCEventListener(c);
                c.addSelectViewEventListener(vv);
                this.subscribeNewMatch(m, c, vv);
                this.updateConnectionStatus(vv.getConnectionInfo(), true);
                vv.onSelectViewEvent(new LoginView(true));
            }
        }else{
            throw new RuntimeException("Are you sure the match has been initiazlied?");
        }
        return noMoreWaitingClients;
    }

    public Queue<VirtualView> getClientsWaitingForMatch(){
        return clientsWaitingForMatch;
    }

    public Map<ConnectionInfo, Boolean> getClientsConnectionStatuses() {
        //return clientsConnectionStatuses;
        synchronized (clientsConnectionStatuses){
            return new HashMap<>(clientsConnectionStatuses);
        }
    }

    public void setClientsConnectionStatuses(ConnectionInfo connectionInfo, boolean status) {
        synchronized (clientsConnectionStatuses){
            this.clientsConnectionStatuses.put(connectionInfo, status);
        }
    }

    public Map<Match, Controller> getMacthesControllers() {
        //return macthesControllers;
        synchronized (macthesControllers){
            return new HashMap<>(macthesControllers);
        }
    }

    public Map<Match, List<VirtualView>> getMatchesViews() {
        //return matchesViews;
        synchronized (matchesViews) {
            return new HashMap<>(matchesViews);
        }
    }

    public void updateConnectionStatus(ConnectionInfo connectionInfo, boolean status){
        synchronized (clientsConnectionStatuses){
            /*if(!clientsConnectionStatuses.containsKey(connectionInfo)) {
                clientsConnectionStatuses.put(connectionInfo, status);
                return;
            }
            for(ConnectionInfo ci : clientsConnectionStatuses.keySet()){
                if(ci.getSignature().equals(connectionInfo.getSignature())){
                    //clientsConnectionStatuses.put(ci, status);
                    //clientsConnectionStatuses.remove(ci);
                    ci.setNickname(connectionInfo.getNickname());
                    clientsConnectionStatuses.put(ci, status);
                }
            }*/
            clientsConnectionStatuses.put(connectionInfo, status);
        }
    }

    public boolean wasConnectedAndHasDisconnected(ConnectionInfo connectionInfo){
        synchronized (clientsConnectionStatuses){
            if(!clientsConnectionStatuses.containsKey(connectionInfo))
                return false;
            for(ConnectionInfo ci : clientsConnectionStatuses.keySet()){
                if(ci.getSignature().equals(connectionInfo.getSignature())){
                    return clientsConnectionStatuses.get(ci)==false;
                }
            }
            return false;
        }
    }

    /*
    public boolean atLeastOneDisconnected(){
        synchronized (clientsConnectionStatuses){
            for(ConnectionInfo ci : clientsConnectionStatuses.keySet()){
                if(clientsConnectionStatuses.get(ci)==false)
                    return true;
            }
            return false;
        }

    }*/
}
