package network;

import configuration.Configuration;
import connection.ConnectionToServer;
import contract.WARMessage;
import domain.Player;
import service.WARService;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class Server {
    private ServerSocket serverSocket;
    private ServerThread waitingPlayerThread = null;
    private ServerThread followerThread = null;
    private WARService warService;
    private String serverType;
    private ConnectionToServer connectionToServer;
    private WARMessage file = new WARMessage((byte) 0, new byte[]{0});

    /**
     * Initiates a server socket on the input port, listens to the line, on receiving an incoming
     * connection creates and starts a ServerThread on the client
     *
     * @param port port to open a socket on
     */
    public Server(int port, String serverType) throws Exception {
        this.serverType = serverType;
        if(serverType.equalsIgnoreCase("Master")){
            warService = WARService.getInstance();
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Opened up a server socket on " + Inet4Address.getLocalHost());
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("network.Server class.Constructor exception on opening a server socket");
            }
            while (true) {
                listenAndAccept();
            }
        } else if(serverType.equalsIgnoreCase("Follower")){
            connectionToServer = new ConnectionToServer(Configuration.getInstance().getProperty("server.address"), port);
            connectionToServer.send(new WARMessage((byte) 6,null));
            while(true){
                int hashCode = connectionToServer.waitForAnswer().getPayload()[0];
                if(hashCode == file.hashCode())
                    connectionToServer.sendForAnswer(new WARMessage((byte)6,"CONSISTENCY_CHECK_PASSED".getBytes()));
                else
                    connectionToServer.sendForAnswer(new WARMessage((byte)6,"RETRANSMIT".getBytes()));
                //communicate();
            }
        } else {
            throw new Exception("Not a server type");
        }

    }

    /**
     * Listens to the line and starts a connection on receiving a request from the client
     * The connection is started and initiated as a ServerThread object
     */
    private void listenAndAccept() {
        Socket socket;
        try {
            socket = serverSocket.accept();
            ServerThread serverThread = new ServerThread(socket);
            serverThread.start();
            WARMessage firstMessage = serverThread.getWARMessage();
            if(firstMessage == null){
                System.out.println("A connection was established with a client on the address of " + socket.getRemoteSocketAddress());
                Player newPlayer = new Player();
                serverThread.setPlayer(newPlayer);
                warService.registerPlayer(newPlayer, serverThread);
                if (waitingPlayerThread == null) {
                    waitingPlayerThread = serverThread;
                } else {
                    if (waitingPlayerThread.isSocketOpen()) {
                        WARMessage matchmakingMessage = new WARMessage((byte) 5, null);
                        waitingPlayerThread.sendWARMessage(matchmakingMessage);
                        serverThread.sendWARMessage(matchmakingMessage);
                        WARService.getInstance().initializeGame(waitingPlayerThread.getPlayer(), serverThread.getPlayer());
                        waitingPlayerThread = null;
                    } else {
                        waitingPlayerThread = serverThread;
                    }
                }
            } else if (firstMessage.getType() == 6){
                System.out.println("A connection was established with a follower on the address of " + socket.getRemoteSocketAddress());


            }else
                throw new Exception("Not accepted");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("network.Server Class.Connection establishment error inside listen and accept function");
        }
    }

    private void communicate() {
    }



}

