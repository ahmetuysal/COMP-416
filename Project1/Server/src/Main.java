import configuration.Configuration;
import controller.ControllerThread;
import network.Server;

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
        ControllerThread controllerThread = ControllerThread.getInstance();
        controllerThread.start();
        System.out.println("Server Type:");
        Scanner sc = new Scanner(System.in);
        String serverType = sc.nextLine();
        try {
            new Server(Integer.parseInt(Configuration.getInstance().getProperty("server.port")), serverType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
