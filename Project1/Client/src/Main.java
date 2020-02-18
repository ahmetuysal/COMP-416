import configuration.Configuration;
import contract.WARMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class Main {

    public static void main(String[] args) {
        try (InputStream inputStream = new FileInputStream("resources/configuration.properties")) {
            Configuration.loadProperties(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConnectionToServer connectionToServer = new ConnectionToServer(Configuration.getInstance().getProperty("server.address"),
                Integer.parseInt(Configuration.getInstance().getProperty("server.port")));
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a message for the echo");
        String message = scanner.nextLine();
        while (!message.equals("QUIT"))
        {
            WARMessage warMessage = new WARMessage((byte) 1, new byte[]{2});
            System.out.println("Response from server: " + connectionToServer.sendForAnswer(warMessage));
            message = scanner.nextLine();
        }
        connectionToServer.disconnect();
    }
}
