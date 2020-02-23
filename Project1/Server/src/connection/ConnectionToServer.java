package connection;

import domain.WARMessage;

import java.io.*;
import java.net.Socket;

/**
 * ConnectionToServer class is responsible from message transmission between Master Server and Follower Server entities.
 * This class is only used in Follower mode to establish a connection with the Master server.
 *
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class ConnectionToServer {
    protected ObjectInputStream objectInputStream;
    protected ObjectOutputStream objectOutputStream;
    private Socket socket;

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     *
     * @param address IP address of the master server
     * @param port    port number of the server
     */
    public ConnectionToServer(String address, int port) {
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
     * Receives the {@code File} that contains information about a single game.
     *
     * @return the {@code File} sent by master server
     */
    public File receiveFile() {
        File file = new File("Received.json");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            //BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String ch;

            while (dataInputStream.available() > 0) {
                System.out.println("of");
                ch = dataInputStream.readUTF();
                System.out.println("ch " + ch);
                fileOutputStream.write(Integer.parseInt(ch));
            }
            fileOutputStream.close();
            fileInputStream.close();
            dataInputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
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
