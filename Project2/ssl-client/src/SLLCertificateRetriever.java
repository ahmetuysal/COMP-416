import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Ahmet Uysal @ahmetuysal
 */
public class SLLCertificateRetriever {
    private static final int BUFFER_SIZE = 4096;
    private Socket socket;
    private DataInputStream dataInputStream;
    private BufferedReader bufferedReader;
    private PrintWriter outputStreamPrintWriter;
    private boolean socketClosedByServer;

    public SLLCertificateRetriever(String address, int port) {
        connect(address, port);
    }

    public boolean retrieveCertificateFromServer() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println(bufferedReader.readLine());
            System.out.print("Please enter your username: ");
            String username = scanner.nextLine();
            System.out.print("Please enter your password: ");
            String password = scanner.nextLine();
            outputStreamPrintWriter.println(username + " " + password);
            outputStreamPrintWriter.flush();
            String response = bufferedReader.readLine();
            while (! "You are successfully authenticated, sending the certificate".equals(response)) {
                System.out.println(response);
                System.out.print("Please enter your username: ");
                username = scanner.nextLine();
                System.out.print("Please enter your password: ");
                password = scanner.nextLine();
                outputStreamPrintWriter.println(username + " " + password);
                outputStreamPrintWriter.flush();
                response = bufferedReader.readLine();
            }
            receiveCertificateFile();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void connect(String address, int port) {
        try {
            socket = new Socket(address, port);
            dataInputStream = new DataInputStream(socket.getInputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
            outputStreamPrintWriter = new PrintWriter(socket.getOutputStream());
        } catch (EOFException e) {
            this.socketClosedByServer = true;
            disconnect();
        } catch (IOException e) {
            System.err.println("Error: no server has been found on " + address + "/" + port);
        }
    }

    private void receiveCertificateFile() {
        int count;
        byte[] buffer = new byte[BUFFER_SIZE];
        try (FileOutputStream fileOutputStream = new FileOutputStream("clientkeystore")) {
            // TODO: what is wrong here?
            while ((count = dataInputStream.read(buffer)) > 0) {
                System.out.println(count);
                fileOutputStream.write(buffer, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Disconnects the socket and closes the buffers
     */
    public void disconnect() {
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (outputStreamPrintWriter != null) {
                socket.getOutputStream().close();
            }
            if (socket != null) {
                socket.close();
            }
            System.out.println("Connection Closed");
        } catch (IOException e) {
            this.socketClosedByServer = true;
            System.out.println("Connection Already Closed by server");
        } finally {
            dataInputStream = null;
            outputStreamPrintWriter = null;
            socket = null;
        }
    }

}
