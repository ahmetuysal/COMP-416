package network;

import contract.WARMessage;
import controller.ControllerThread;
import domain.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class ServerThread extends Thread {

    private LinkedBlockingQueue<WARMessage> outgoingQueue;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Socket socket;
    private Player player;

    /**
     * @param socket Input socket to create a thread on
     */
    public ServerThread(Socket socket, Player player) {
        this.socket = socket;
        this.player = player;
        this.outgoingQueue = new LinkedBlockingQueue<>();
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
                if (!this.outgoingQueue.isEmpty()) {
                    WARMessage outgoingMessage = this.outgoingQueue.poll();
                    this.sendWARMessage(outgoingMessage);
                }
                warMessage = (WARMessage) objectInputStream.readObject();
                ControllerThread.getInstance().queueIncomingWARMessage(warMessage, this.player);
                System.out.println("Client " + socket.getRemoteSocketAddress() + " sent : " + warMessage.toString());
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

    public void sendWARMessage(WARMessage message) {
        try {
            objectOutputStream.writeObject(message);
            System.out.println("Response " + message.toString() + " sent to client: " + socket.getRemoteSocketAddress());
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
