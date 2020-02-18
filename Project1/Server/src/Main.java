import configuration.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
        new Server(Integer.parseInt(Configuration.getInstance().getProperty("server.port")));
    }
}
