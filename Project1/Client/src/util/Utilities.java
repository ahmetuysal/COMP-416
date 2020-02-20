package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public final class Utilities {

    private Utilities() {
    }

    public static List<Byte> byteArrayToByteList(byte[] byteArray) {
        List<Byte> byteList = new ArrayList<>();
        for (Byte value : byteArray) {
            byteList.add(value);
        }
        return byteList;
    }

}
