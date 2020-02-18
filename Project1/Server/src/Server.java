import controller.ControllerThread;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class Server {
    private ServerSocket serverSocket;
    private ControllerThread waitingPlayerThread = null;

    /**
     * Initiates a server socket on the input port, listens to the line, on receiving an incoming
     * connection creates and starts a ServerThread on the client
     *
     * @param port port to open a socket on
     */
    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Opened up a server socket on " + Inet4Address.getLocalHost());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Server class.Constructor exception on opening a server socket");
        }
        while (true) {
            ListenAndAccept();
        }
    }

    /**
     * Listens to the line and starts a connection on receiving a request from the client
     * The connection is started and initiated as a ServerThread object
     */
    private void ListenAndAccept() {
        Socket socket;
        try {
            socket = serverSocket.accept();
            System.out.println("A connection was established with a client on the address of " + socket.getRemoteSocketAddress());
            ControllerThread controllerThread = new ControllerThread(socket);
            controllerThread.start();
            if (waitingPlayerThread == null) {
                waitingPlayerThread = controllerThread;
            }
            else {
                if (waitingPlayerThread.isSocketOpen()) {
                    waitingPlayerThread.sendMatchmakingMessage();
                    controllerThread.sendMatchmakingMessage();
                    waitingPlayerThread = null;
                }
                else {
                    waitingPlayerThread = controllerThread;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Server Class.Connection establishment error inside listen and accept function");
        }
    }

}

