package follower;

import java.io.*;
import java.net.Socket;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class DataConnectionToServer {
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Socket socket;
    private static final int BUFFER_SIZE = 4096;


    public DataConnectionToServer(String address, int port) {
        connect(address, port);
    }

    private void connect(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
        } catch (IOException e) {
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }

    public void receiveFile(String fileName) {
        int count;
        byte[] buffer = new byte[BUFFER_SIZE];
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            while ((count = dataInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
