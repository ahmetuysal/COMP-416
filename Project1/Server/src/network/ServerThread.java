package network;

import controller.ControllerThread;
import domain.Correspondent;
import domain.WARMessage;
import service.WARService;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A ServerThread object manages communication between the master server and a correspondent using the given socket.
 *
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class ServerThread extends Thread {

    private static final int BUFFER_SIZE = 4096;
    private LinkedBlockingQueue<WARMessage> outgoingQueue;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    // these three fields are created for HTTP and not used in our WAR protocol implementation
    private boolean isHttpProtocol = false;
    private BufferedReader in;
    private PrintWriter out;

    private Socket socket;
    private Correspondent correspondent;
    private boolean isThreadKilled = false;


    /**
     * Creates a ServerThread object that handles communication using given socket
     *
     * @param socket the socket to communicate
     */
    public ServerThread(Socket socket) {
        this.socket = socket;
        this.outgoingQueue = new LinkedBlockingQueue<>();
    }

    /**
     * Returns whether the socket is open
     *
     * @return {@code true} is the socket is open, {@code false} otherwise
     */
    public boolean isSocketOpen() {
        return this.socket.isConnected() && !this.socket.isClosed();
    }

    /**
     * Returns the {@code Correspondent} which represents the entity we communicate using this server thread
     *
     * @return the {@code Correspondent} that represents the entity we communicate using this server thread
     */
    public Correspondent getCorrespondent() {
        return correspondent;
    }

    /**
     * Sets the {@code Correspondent} of this ServerThread
     *
     * @param correspondent the {@code Correspondent} of this ServerThread
     */
    public void setCorrespondent(Correspondent correspondent) {
        this.correspondent = correspondent;
    }

    public void run() {
        try {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (StreamCorruptedException e) {
            // this is from a browser, we couldn't finish the HTTP implementation
            this.isHttpProtocol = true;
            try {
                this.in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                this.out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())),
                        true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isHttpProtocol) {
            try {
                handleHttpConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            WARMessage warMessage;
            while (!isThreadKilled) {
                if (!this.outgoingQueue.isEmpty()) {
                    WARMessage outgoingMessage = this.outgoingQueue.poll();
                    this.sendWARMessage(outgoingMessage);
                }
                warMessage = (WARMessage) objectInputStream.readObject();
                ControllerThread.getInstance().queueIncomingWARMessage(warMessage, this);
                System.out.println("Client " + socket.getRemoteSocketAddress() + " sent : " + warMessage.toString());
            }
        } catch (IOException e) {
            WARService.getInstance().handleTermination(this.correspondent);
            System.err.println("Server Thread. Run. IO Error/ Client " + this.getName() + " terminated abruptly");
        } catch (NullPointerException e) {
            System.err.println("Server Thread. Run.Client " + this.getName() + " Closed");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            terminate();
        }
    }

    // Not used in WAR Protocol & implementation. Unsuccessful attempt to serve on HTTP
    private void handleHttpConnection() throws IOException {
        String s;
        while ((s = in.readLine()) != null) {
            System.out.println(s);
        }
        out.write(getHttpRepresentation());
        out.flush();
    }

    // Not used in WAR Protocol & implementation. Unsuccessful attempt to serve on HTTP
    private String getHttpRepresentation() {
        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='utf-8'>\n" +
                "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>\n" +
                "    <title>WAR Game</title>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1'>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Please wait until an opponent is connected</h1>\n" +
                "  <form>\n" +
                "        <input title=\"Your name\">\n" +
                "        <button type=\"submit\">Send</button>\n" +
                "    </form>" +
                "</body>\n" +
                "</html>";

        String response = "HTTP/1.0 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + content.length() + "\r\n\r\n" + content;
        return response;
    }

    /**
     * Sends a {@code WARMessage} object to {@code Correspondent}
     *
     * @param message the {@code WARMessage} object to send
     */
    public void sendWARMessage(WARMessage message) {
        try {
            objectOutputStream.writeObject(message);
            System.out.println("Response " + message.toString() + " sent to client: " + socket.getRemoteSocketAddress());
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a file to {@code Correspondent}
     *
     * @param file the file to send
     */
    public void sendFile(File file) {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            OutputStream out = objectOutputStream;
            byte[] bytes = new byte[BUFFER_SIZE];
            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Terminates this connection and closes the socket
     */
    public void terminate() {
        try {
            System.out.println("Closing the connection");
            if (objectInputStream != null) {
                objectInputStream.close();
                System.err.println("Socket Input Stream Closed");
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
                System.err.println("Socket Out Closed");
            }
            if (socket != null) {
                socket.close();
                System.err.println("Socket Closed");
            }
        } catch (IOException ie) {
            System.err.println("Socket Close Error");
        }
        isThreadKilled = true;
    }

}
