package util;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains utility functions that are not directly connected to game logic but used in implementation
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public final class Utilities {

    private Utilities() {
    }

    /**
     * Returns a list of byte that contain the same byte with the given byte array
     * @param byteArray byte array that will be converted to byte list
     * @return List of byte who stores the same content with the given byte array
     */
    public static List<Byte> byteArrayToByteList(byte[] byteArray) {
        List<Byte> byteList = new ArrayList<>();
        for (Byte value : byteArray) {
            byteList.add(value);
        }
        return byteList;
    }

}
