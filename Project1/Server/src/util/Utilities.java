package util;

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

}
