package network;

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
    private WARService warService;

    /**
     * Initiates a server socket on the input port, listens to the line, on receiving an incoming
     * connection creates and starts a ServerThread on the client
     *
     * @param port port to open a socket on
     */
    public Server(int port) {
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
    }

    /**
     * Listens to the line and starts a connection on receiving a request from the client
     * The connection is started and initiated as a ServerThread object
     */
    private void listenAndAccept() {
        Socket socket;
        try {
            socket = serverSocket.accept();
            System.out.println("A connection was established with a client on the address of " + socket.getRemoteSocketAddress());
            Player newPlayer = new Player();
            ServerThread serverThread = new ServerThread(socket, newPlayer);
            serverThread.start();
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
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("network.Server Class.Connection establishment error inside listen and accept function");
        }
    }

}

