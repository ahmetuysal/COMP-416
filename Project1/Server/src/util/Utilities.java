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

    // Implementing Fisher–Yates shuffle
    public static void shuffleByteArray(byte[] byteArray) {
        Random rnd = ThreadLocalRandom.current();
        for (int i = byteArray.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            byte a = byteArray[index];
            byteArray[index] = byteArray[i];
            byteArray[i] = a;
        }
    }

    public static byte[] byteListToByteArray(List<Byte> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }

    public static List<Byte> byteArrayToByteList(byte[] byteArray) {
        List<Byte> byteList = new ArrayList<>();
        for (Byte value : byteArray) {
            byteList.add(value);
        }
        return byteList;
    }

}
