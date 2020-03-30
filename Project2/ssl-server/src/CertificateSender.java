import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Ahmet Uysal @ahmetuysal
 */
public class CertificateSender extends Thread {

    private final int port;
    private ServerSocket serverSocket;

    public CertificateSender(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            listenAndAccept();
        }
    }

    /**
     * Listens to the line and starts a connection on receiving a request with the client
     */
    private void listenAndAccept() {
        Socket socket;
        try {
            socket = serverSocket.accept();
            System.out.println("A connection was established with a client on the address of " + socket.getRemoteSocketAddress());
            CertificateSenderThread certificateSenderThread = new CertificateSenderThread(socket);
            certificateSenderThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("CertificateSender. Connection establishment error inside listen and accept function");
        }
    }
}
