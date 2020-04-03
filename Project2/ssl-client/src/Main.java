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
    public final static int TLS_SERVER_PORT = 1024 + (60780 + 60124 + 60045) % 64512;
    public final static int TLS_CERTIFICATE_PORT = 4444;

    public static void main(String[] args) throws Exception {

        SLLCertificateRetriever sllCertificateRetriever = new SLLCertificateRetriever(TLS_SERVER_ADDRESS, TLS_CERTIFICATE_PORT);

        boolean isCertificateRetrieved = sllCertificateRetriever.retrieveCertificateFromServer();

        if (!isCertificateRetrieved) {
            throw new Exception("Cannot get the ssl certificate of the server.");
        }

        // Creates an SSLConnectToServer object on the specified server address and port
        SSLConnectToServer sslConnectToServer = new SSLConnectToServer(TLS_SERVER_ADDRESS, TLS_SERVER_PORT);

        sslConnectToServer.connect();
        String chars = sslConnectToServer.retrieveEmailAddressCharactersAtIndex(0);
        sslConnectToServer.disconnect();

        StringBuilder[] stringBuilders = new StringBuilder[chars.length()];
        for (int i = 0; i < chars.length(); i++) {
            stringBuilders[i] = new StringBuilder(String.valueOf(chars.charAt(i)));
        }
        System.out.println(chars);

        for (int i = 1; ; i++) {
            sslConnectToServer.connect();
            chars = sslConnectToServer.retrieveEmailAddressCharactersAtIndex(i);
            sslConnectToServer.disconnect();
            if (chars == null || chars.equals("")) {
                break;
            }
            System.out.println(chars);
            for (int j = 0; j < chars.length(); j++) {
                stringBuilders[j].append(chars.charAt(j));
            }
        }

        for (StringBuilder sb : stringBuilders) {
            System.out.println(sb.toString());
        }

    }
}
