package connection;

import domain.WARMessage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * ConnectionToServer class is responsible from message transmission between Client and Master Server
 *
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class ConnectionToServer {
    protected ObjectInputStream objectInputStream;
    protected ObjectOutputStream objectOutputStream;
    private Socket socket;
    private boolean socketClosedByServer;

    /**
     * Establishes a socket connection to the server that is identified by the {@code address} and the {@code port}
     *
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
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
            this.socketClosedByServer = false;
            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
        } catch (EOFException e) {
            this.socketClosedByServer = true;
            disconnect();
        } catch (IOException e) {
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }

    /**
     * Retrieves a {@code WARMessage} object from the server
     *
     * @return {@code WARMessage} that server sent
     */
    public WARMessage waitForAnswer() {
        WARMessage response = null;
        try {
            response = (WARMessage) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }


    /**
     * Sends a {@code WARMessage} object to the server and retrieves the answer {@code WARMessage}
     *
     * @param message {@code WARMessage} object that will be sent to server
     * @return {@code WARMessage} that server sent
     */
    public WARMessage sendForAnswer(WARMessage message) {
        WARMessage response = null;
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            response = (WARMessage) objectInputStream.readObject();
        } catch (EOFException | SocketException e) {
            this.socketClosedByServer = true;
            disconnect();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Socket read Error");
        }
        return response;
    }

    /**
     * Sends a {@code WARMessage} object to the server, does not wait for the answer
     */
    public void send(WARMessage message) {
        try {
            System.out.println("Sending message: " + message.toString());
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (SocketException e) {
            this.socketClosedByServer = true;
            disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnects the socket and closes the buffers
     */
    public void disconnect() {
        try {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
            socketClosedByServer = true;
            System.out.println("Connection Closed");
        } catch (IOException e) {
            System.out.println("Connection Already Closed by server");
        } finally {
            objectInputStream = null;
            objectOutputStream = null;
            socket = null;
        }
    }

    /**
     * Returns whether the connection with the server is active
     * @return {@code true} if connection is still active, {@code false} otherwise
     */
    public boolean isConnectionActive() {
        return !socketClosedByServer && this.socket != null && this.socket.isConnected() && !this.socket.isClosed();
    }

}

