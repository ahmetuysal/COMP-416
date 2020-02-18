import contract.WARMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class ConnectionToServer {
    protected ObjectInputStream objectInputStream;
    protected ObjectOutputStream objectOutputStream;
    private Socket socket;

    /**
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port    port number of the server
     */
    public ConnectionToServer(String address, int port) {
        connect(address, port);
    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     */
    private void connect(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
        } catch (IOException e) {
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }

    /**
     * sends the message String to the server and retrieves the answer
     *
     * @param message input message string to the server
     * @return the received server answer
     */
    public WARMessage sendForAnswer(WARMessage message) {
        WARMessage response = null;
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            response = (WARMessage) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("ConnectionToServer. SendForAnswer. Socket read Error");
        }
        return response;
    }


    /**
     * Disconnects the socket and closes the buffers
     */
    public void disconnect() {
        try {
            objectInputStream.close();
            objectOutputStream.close();
            socket.close();
            System.out.println("ConnectionToServer. SendForAnswer. Connection Closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
