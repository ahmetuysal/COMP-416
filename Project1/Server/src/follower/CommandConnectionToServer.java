package follower;

import domain.WARMessage;
import util.Utilities;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

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
    private int ID = 0;

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     * Defines the ID number of the current follower
     *
     * @param address IP address of the master server
     * @param port    port number of the server
     */
    public CommandConnectionToServer(String address, int port) {
        connect(address, port);
        while (new File("Follower-" + ID).exists()) {
            ID++;
        }
        new File("Follower-" + ID).mkdir();
    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     *
     * @param serverAddress IP address of the master server
     * @param serverPort port number of the server
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

    /**
     *
     * Takes a fileName, opens the file and writes the content that server has sent into the file.
     *
     * @param fileName File name value to write into.
     */
    public void receiveFile(String fileName) {
        int count;
        byte[] buffer = new byte[BUFFER_SIZE];
        try (FileOutputStream fileOutputStream = new FileOutputStream("Follower-" + ID + "/" + fileName)) {
            while ((count = objectInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param checksum A checksum value
     * @param fileName Name of a file to calculate and compare the checksum
     * @return true if the checksum is equal to the calculated checksum of the file with the name fileName, else false
     */
    public boolean compareChecksumWithFile(byte[] checksum, String fileName) {
        return Arrays.equals(Utilities.calculateFileChecksum(new File("Follower-" + ID + "/" + fileName)), checksum);
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
