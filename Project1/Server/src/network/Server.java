package network;

import configuration.Configuration;
import connection.ConnectionToServer;
import contract.WARMessage;
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
    private WARService warService;
    private WARMessage file = new WARMessage((byte) 0, new byte[]{0});

    /**
     * Initiates a server socket on the input port, listens to the line, on receiving an incoming
     * connection creates and starts a ServerThread on the client
     *
     * @param port port to open a socket on
     */
    public Server(int port, String serverType) throws Exception {
        if (serverType.equalsIgnoreCase("Master")) {
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
        } else if (serverType.equalsIgnoreCase("Follower")) {
            ConnectionToServer connectionToServer = new ConnectionToServer(Configuration.getInstance().getProperty("server.address"), port);
            connectionToServer.send(new WARMessage((byte) 6, null));
            while (true) {
                int hashCode = connectionToServer.waitForAnswer().getPayload()[0];
                if (hashCode == file.hashCode())
                    connectionToServer.sendForAnswer(new WARMessage((byte) 6, "CONSISTENCY_CHECK_PASSED".getBytes()));
                else
                    connectionToServer.sendForAnswer(new WARMessage((byte) 6, "RETRANSMIT".getBytes()));
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
            System.out.println("A connection was established with a correspondent on the address of " + socket.getRemoteSocketAddress());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("network.Server Class.Connection establishment error inside listen and accept function");
        }
    }

    private void communicate() {
    }


}

