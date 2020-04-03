import java.io.*;
import java.net.Socket;

/**
 * @author Ahmet Uysal @ahmetuysal
 */
public class CertificateSenderThread extends Thread {
    private static final int BUFFER_SIZE = 4096;

    private Socket socket;
    private PrintWriter outputStreamPrintWriter;
    private BufferedReader bufferedReader;

    public CertificateSenderThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            outputStreamPrintWriter = new PrintWriter(socket.getOutputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Authentication using users.txt file
        boolean isAuthenticated = false;

        try {
            do {
                String clientCredentials = askForCredentials();
                if ("".equals(clientCredentials)) {
                    return;
                }
                BufferedReader credentialsFileReader = new BufferedReader(new FileReader("src/users.txt"));
                String line = credentialsFileReader.readLine();
                while (line != null) {
                    if (line.equals(clientCredentials)) {
                        isAuthenticated = true;
                        break;
                    }
                    line = credentialsFileReader.readLine();
                }
                credentialsFileReader.close();
            } while (!isAuthenticated);

        } catch (IOException e) {
            e.printStackTrace();
        }

        outputStreamPrintWriter.println("You are successfully authenticated, sending the certificate");
        outputStreamPrintWriter.flush();

        // send the certificate
        sendCertificateFile();
        // terminate this thread
    }


public void sendCertificateFile() {
    File certificateFile = new File("server_crt.crt");
    try {
        System.out.println("Start sending the file");
        FileInputStream certificateFileInputStream = new FileInputStream(certificateFile);
        byte[] bytes = new byte[BUFFER_SIZE];
        int count;
        while ((count = certificateFileInputStream.read(bytes)) > 0) {
            socket.getOutputStream().write(bytes, 0, count);
        }
        // send EOF character
        socket.getOutputStream().write(26);
        System.out.println("Done sending the file");
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    /**
     * Sends a prompt message to client and receives credentials of the client
     *
     * @return credentials of the user, i.e, username and password separated by a space character
     */
    public String askForCredentials() {
        String response = "";
        try {
            outputStreamPrintWriter.println("You are connected to the server. Please send your credentials (username and password separated by a single space character) to receive the SSL Certificate");
            outputStreamPrintWriter.flush();
            response = bufferedReader.readLine();
            System.out.println(response);
        } catch (IOException e) {
            System.err.println("Client on address " + socket.getRemoteSocketAddress() + " disconnected, executing the certificate sender thread.");
        }
        return response;
    }


}
