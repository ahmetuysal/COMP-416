package network;

import contract.WARMessage;
import controller.ControllerThread;
import domain.Correspondent;
import domain.Player;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class ServerThread extends Thread {

    private LinkedBlockingQueue<WARMessage> outgoingQueue;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Socket socket;
    private Correspondent correspondent;


    public ServerThread(Socket socket) {
        this.socket = socket;
        this.outgoingQueue = new LinkedBlockingQueue<>();
    }

    public boolean isSocketOpen() {
        return this.socket.isConnected() && !this.socket.isClosed();
    }

    public Correspondent getCorrespondent() {
        return correspondent;
    }

    public void setCorrespondent(Correspondent correspondent) {
        this.correspondent = correspondent;
    }

    public void run() {
        try {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Scanner sc = new Scanner(System.in);
        String exitMessage = sc.nextLine();
        if(exitMessage.equalsIgnoreCase("Exit")){
            File file = new File("WARGame.json");
            if(file.exists())
                file.delete();
            System.exit(0);
        }*/

        try {
            WARMessage warMessage;
            while (true) {
                if (!this.outgoingQueue.isEmpty()) {
                    WARMessage outgoingMessage = this.outgoingQueue.poll();
                    this.sendWARMessage(outgoingMessage);
                }
                warMessage = (WARMessage) objectInputStream.readObject();
                ControllerThread.getInstance().queueIncomingWARMessage(warMessage, this);
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
                // TODO: client has left, terminate the game if it's not finished
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

   public void sendFile(File file) {
       FileInputStream fis = null;
       try {
           byte[] mybytearray = new byte[(int) file.length() + 1];
           fis = new FileInputStream(file);
           BufferedInputStream bis = new BufferedInputStream(fis);
           bis.read(mybytearray, 0, mybytearray.length);
           objectOutputStream.write(mybytearray, 0, mybytearray.length);
           objectOutputStream.flush();
           bis.close();
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

}
