import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 * Copyright [2017] [Yahya Hassanzadeh-Nazarabadi]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This class handles and establishes an SSL connection to a server
 */
public class SSLConnectToServer {
    /*
    Name of key store file
     */
    private final String KEY_STORE_NAME = "clientkeystore";
    /*
    Password to the key store file
     */
    private final String KEY_STORE_PASSWORD = "storepass";
    protected String serverAddress;
    protected int serverPort;
    private SSLSocket sslSocket;
    private BufferedReader is;
    private PrintWriter os;


    public SSLConnectToServer(String address, int port) throws Exception {

        serverAddress = address;
        serverPort = port;
        /*
        Loads the keystore's address of client
         */
        System.setProperty("javax.net.ssl.trustStore", KEY_STORE_NAME);

        // Loads the keystore's password of client
        System.setProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASSWORD);

        // Load the certificates in the key store
        createKeyStore();
    }

    public void createKeyStore() throws Exception {

        // Create keystore instance
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        // Load the empty keystore
        ks.load(null, null);

        // Initialize the certificate factory
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Load the certificate
        InputStream caInput = new BufferedInputStream(new FileInputStream(new File("server_crt.crt")));

        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            // System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        // Create the certificate entry
        ks.setCertificateEntry("ca", ca);

        // Write the file back
        ks.store(new FileOutputStream("clientkeystore"), KEY_STORE_PASSWORD.toCharArray());

    }

    /**
     * Connects to the specified server by serverAddress and serverPort
     */
    public void connect() {
        try {
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(serverAddress, serverPort);
            sslSocket.startHandshake();
            is = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            os = new PrintWriter(sslSocket.getOutputStream());
            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Disconnects form the specified server
     */
    public void disconnect() {
        try {
            is.close();
            os.close();
            sslSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message as a string over the secure channel and receives
     * answer from the server
     * @param message input message
     * @return response from server
     */
    public String sendForAnswer(String message) {
        String response = "";
        try {
            os.println(message);
            os.flush();
            response = is.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ConnectionToServer. SendForAnswer. Socket read Error");
        }
        return response;
    }


}
