package configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Wrapper singleton class around Properties for accessing user related parameters such as MongoDB connection properties,
 * port number, etc. stored in configuration.properties file.
 *
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class Configuration extends Properties {

    private static Configuration _instance = new Configuration();

    private Configuration() {
    }

    /**
     * Gets the Configuration object that stored application configurations
     *
     * @return the {@code Configuration} instance to access property values
     */
    public static synchronized Configuration getInstance() {
        return _instance;
    }


    /**
     * Reads a property list (key and element pairs) from the input
     * character stream in a simple line-oriented format.
     *
     * @param inputStream the input character stream.
     * @throws IOException              if an error occurred when reading from the
     *                                  input stream.
     * @throws IllegalArgumentException if a malformed Unicode escape
     *                                  appears in the input.
     * @throws NullPointerException     if {@code inputStream} is null.
     */
    public static void loadProperties(InputStream inputStream) throws IOException {
        _instance.load(inputStream);
    }
}
