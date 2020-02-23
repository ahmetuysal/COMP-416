package util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import domain.WARGame;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public static void writeWARGameToJSON(WARGame warGame) {
        File file = new File(warGame.getPlayer1().getName() + "-" + warGame.getPlayer2().getName() + ".json");
        Gson jsonEncoder = new Gson();
        String player1 = jsonEncoder.toJson(warGame.getPlayer1());
        String player2 = jsonEncoder.toJson(warGame.getPlayer2());
        try {
            JsonWriter wr = new JsonWriter(new FileWriter(file));
            wr.beginObject();
            wr.name("Round Num").value(warGame.getNumRounds());
            wr.name("Players");
            wr.beginArray();
            wr.value(player1);
            wr.value(player2);
            wr.endArray();
            wr.endObject();
            wr.flush();
            wr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteWarGameJSONFile(WARGame warGame) {
        File file = new File(warGame.getPlayer1().getName() + "-" + warGame.getPlayer2().getName() + ".json");
        return file.delete();
    }

    public static byte[] calculateFileChecksum(File file) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try (InputStream in = new FileInputStream(file)) {
            byte[] block = new byte[4096];
            int length;
            while ((length = in.read(block)) > 0) {
                assert messageDigest != null;
                messageDigest.update(block, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert messageDigest != null;
        return messageDigest.digest();
    }

    public static byte[] fileToByteArray(File file) {
        byte[] byteArray = new byte[(int) file.length()];
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(byteArray);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

}
