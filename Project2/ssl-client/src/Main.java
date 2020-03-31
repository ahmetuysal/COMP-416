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
public class Main {
    public final static String TLS_SERVER_ADDRESS = "localhost";
    public final static String MESSAGE_TO_TLS_SERVER = "hello from client";
    public final static int TLS_SERVER_PORT = (4444 + 1 + 2) % 65535;
    public final static int TLS_CERTIFICATE_PORT = 4444;

    public static void main(String[] args) throws Exception {

        SLLCertificateRetriever sllCertificateRetriever = new SLLCertificateRetriever(TLS_SERVER_ADDRESS, TLS_CERTIFICATE_PORT);

        boolean isCertificateRetrieved = sllCertificateRetriever.retrieveCertificateFromServer();

        if (!isCertificateRetrieved) {
            throw new Exception("Cannot get the ssl certificate of the server.");
        }

        // Creates an SSLConnectToServer object on the specified server address and port
        SSLConnectToServer sslConnectToServer = new SSLConnectToServer(TLS_SERVER_ADDRESS, TLS_SERVER_PORT);

        // Connects to the server
        sslConnectToServer.connect();

        // Sends a message over SSL socket to the server and prints out the received message from the server
        System.out.println(sslConnectToServer.sendForAnswer(MESSAGE_TO_TLS_SERVER));

        //Disconnects from the SSL server
        sslConnectToServer.disconnect();
    }
}
