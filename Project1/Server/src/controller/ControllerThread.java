package controller;

import contract.WARMessage;
import domain.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class ControllerThread extends Thread {
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Socket socket;
    private Player player;

    /**
     * @param socket Input socket to create a thread on
     */
    public ControllerThread(Socket socket, Player player) {
        this.socket = socket;
        this.player = player;
    }

    public boolean isSocketOpen() {
        return this.socket.isConnected() && !this.socket.isClosed();
    }

    public Player getPlayer() {
        return player;
    }

    public void run() {
        try {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            WARMessage warMessage;
            while (true) {
                warMessage = (WARMessage) objectInputStream.readObject();
                System.out.println("Client " + socket.getRemoteSocketAddress() + " sent : " + warMessage.toString());
                WARMessage warResponse = handleWARMessage(warMessage);
                objectOutputStream.writeObject(warResponse);
                System.out.println("Response " + warMessage.toString() + " sent to client: " + socket.getRemoteSocketAddress());
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            System.err.println("Server Thread. Run. IO Error/ Client " + this.getName() + " terminated abruptly");
        } catch (NullPointerException e) {
            System.err.println("Server Thread. Run.Client " + this.getName() + " Closed");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
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
        }
    }

    public void sendMatchmakingMessage() {
        try {
            WARMessage matchmakingMessage = new WARMessage((byte) 5, null);
            objectOutputStream.writeObject(matchmakingMessage);
            System.out.println("Response " + matchmakingMessage.toString() + " sent to client: " + socket.getRemoteSocketAddress());
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private WARMessage handleWARMessage(WARMessage warMessage) {
        if (validateWarMessage(warMessage)) {
            // TODO: implement WARMessage handling
            if (warMessage.getType() == 0) {

            }
        } else {
            // TODO: send error message
        }
        return warMessage;
    }


    private boolean validateWarMessage(WARMessage warMessage) {
        // check payload based on message type
        switch (warMessage.getType()) {
            // want game
            case 0:
                // play card
            case 2:
                // play result
            case 3:
                // game result
            case 4:
                if (warMessage.getPayload() == null || warMessage.getPayload().length != 1) {
                    return false;
                }
                break;
            // game start
            case 1:
                if (warMessage.getPayload() == null || warMessage.getPayload().length != 26) {
                    return false;
                }
                break;
            // matchmaking
            case 5:
                return warMessage.getPayload() == null || warMessage.getPayload().length == 0;
            // invalid WARMessage type
            default:
                return false;
        }

        // check card values
        for (Byte card : warMessage.getPayload()) {
            if (card < 0 || card > 51) {
                return false;
            }
        }
        return true;
    }

}
