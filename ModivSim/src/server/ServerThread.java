package server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import domain.Message;

/**
 * A Server.ServerThread object manages communication between the master server and a correspondent using the given socket.
 *
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class ServerThread extends Thread {

    private static final int BUFFER_SIZE = 4096;
    private LinkedBlockingQueue<Message> outgoingQueue;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    private Socket socket;
    private boolean isThreadKilled = false;


    public ServerThread(Socket socket) {
        this.socket = socket;
        this.outgoingQueue = new LinkedBlockingQueue<>();
    }

    public boolean isSocketOpen() {
        return this.socket.isConnected() && !this.socket.isClosed();
    }



    public void run() {

        try {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Message information;
            while (!isThreadKilled) {
                if (!this.outgoingQueue.isEmpty()) {
                    Message outgoingMessage = this.outgoingQueue.poll();
                    this.sendMessage(outgoingMessage);
                }
                information = (Message) objectInputStream.readObject();
                ControllerThread.getInstance().queueIncomingMessage(information, this);
                // System.out.println("Client " + socket.getRemoteSocketAddress() + " sent : " + information.toString());
            }
        } catch (IOException e) {
            // anything here?
            System.err.println("Server Thread. Run. IO Error/ Client " + this.getName() + " terminated abruptly");
        } catch (NullPointerException e) {
            System.err.println("Server Thread. Run.Client " + this.getName() + " Closed");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            terminate();
        }
    }

 void sendMessage(Message message) {
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
