package server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeServer {

    private ServerSocket serverSocket;

    public NodeServer(int port, String serverType) throws Exception {

            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Opened up a server socket on " + Inet4Address.getLocalHost());
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("network.Server class.Constructor exception on opening a server socket");
            }
            while (true)
                listenAndAccept();

    }


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

}
