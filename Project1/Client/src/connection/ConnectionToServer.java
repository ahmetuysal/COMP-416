package connection;

import domain.WARMessage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class ConnectionToServer {
    protected ObjectInputStream objectInputStream;
    protected ObjectOutputStream objectOutputStream;
    private Socket socket;
    private boolean socketClosedByServer;

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
            this.socketClosedByServer = false;
            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
        } catch (EOFException e) {
            this.socketClosedByServer = true;
            disconnect();
        } catch (IOException e) {
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }


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
        } catch (EOFException | SocketException e) {
            this.socketClosedByServer = true;
            disconnect();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Socket read Error");
        }
        return response;
    }

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

    public boolean isConnectionActive() {
        return !socketClosedByServer && this.socket != null && this.socket.isConnected() && !this.socket.isClosed();
    }

}

