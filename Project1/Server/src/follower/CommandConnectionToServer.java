package follower;

import domain.WARMessage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * CommandConnectionToServer class is responsible from message transmission between Master Server and Follower Server entities.
 * This class is only used in Follower mode to establish a connection with the Master server.
 *
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class CommandConnectionToServer {
    private static final int BUFFER_SIZE = 4096;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Socket socket;

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     *
     * @param address IP address of the master server
     * @param port    port number of the server
     */
    public CommandConnectionToServer(String address, int port) {
        connect(address, port);
    }

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
     * Sends the {@code WARMessage} object to the server and retrieves the answer {@code WARMessage} object
     *
     * @param message {@code WARMessage} object that will be send to the server
     * @return the received {@code WARMessage} object
     */
    public WARMessage sendForAnswer(WARMessage message) {
        WARMessage response = null;
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            response = (WARMessage) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Socket read Error");
        }
        return response;
    }


    /**
     * Retrieves the answer {@code WARMessage} object from master server
     *
     * @return the received {@code WARMessage} object
     */
    public WARMessage waitForAnswer() {
        WARMessage response = null;
        try {
            response = (WARMessage) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Socket read Error");
        }
        return response;
    }

    /**
     * Sends the {@code WARMessage} object to the server
     *
     * @param message {@code WARMessage} object that will be send to the server
     */
    public void sendWarMessage(WARMessage message) {
        try {
            System.out.println("Sending message: " + message.toString());
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveFile(String fileName) {
        int count;
        byte[] buffer = new byte[BUFFER_SIZE];
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            while ((count = objectInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Disconnects the socket and closes the object input and output streams
     */
    public void disconnect() {
        try {
            objectInputStream.close();
            objectOutputStream.close();
            socket.close();
            System.out.println("Connection Closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
