package configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class Configuration extends Properties {
    private static Configuration _instance = new Configuration();

    public static synchronized Configuration getInstance() {
        return _instance;
    }

    public static void loadProperties(InputStream inputStream) throws IOException {
        _instance.load(inputStream);
    }
}
