package network;

import configuration.Configuration;
import domain.WARMessage;
import follower.CommandConnectionToServer;
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
    private CommandConnectionToServer commandConnectionToServer;


    /**
     *
     * If serverType is master, initiates a server socket on the input port, listens to the line, on receiving an incoming
     * connection creates and starts a ServerThread on the client
     * If serverType is follower, sends a message to the master server to indicates its type
     * and listens to get files from the master server
     *
     * @param port port to open a socket on
     * @param serverType the value to check if the server is master or follower
     * @throws Exception not a server type if the input is not "master" or "follower"
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
            while (true)
                listenAndAccept();
        } else if (serverType.equalsIgnoreCase("Follower")) {
            commandConnectionToServer = new CommandConnectionToServer(Configuration.getInstance().getProperty("server.address"), port);
            WARMessage iAmFollowerMessage = new WARMessage((byte) 6, new byte[]{1});
            commandConnectionToServer.sendWarMessage(iAmFollowerMessage);
            while (true)
                communicate();
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

    /**
     * Listens and receives a file from the server.
     * Checks if the file is valid and sends and answer back to the server.
     */
    private void communicate() {
        WARMessage receiveFileMessage = commandConnectionToServer.waitForAnswer();
        if (receiveFileMessage.getType() == 8) {
            String fileName = new String(receiveFileMessage.getPayload());
            commandConnectionToServer.receiveFile(fileName);
            WARMessage fileHashMessage = commandConnectionToServer.waitForAnswer();
            boolean checksumValidation = commandConnectionToServer.compareChecksumWithFile(fileHashMessage.getPayload(), fileName);
            System.out.println("Checksum validation: " + checksumValidation);
            if (checksumValidation)
                commandConnectionToServer.sendWarMessage(new WARMessage((byte) 9, ("CONSISTENCY_CHECK_PASSED " + fileName).getBytes()));
            else
                commandConnectionToServer.sendWarMessage(new WARMessage((byte) 9, ("RETRANSMIT " + fileName).getBytes()));
        }


    }
}

