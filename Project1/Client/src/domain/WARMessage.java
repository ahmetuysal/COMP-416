package domain;

import java.io.Serializable;
import java.util.Arrays;

/**
 * WARMessage is the main transfer object of the WAR Protocol. It consist of a {@code type} and {@code payload}
 *
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class WARMessage implements Serializable {
    private static final long serialVersionUID = -5817893373429527297L;
    private final byte type;
    private final byte[] payload;

    /**
     * Instantiates a new {@code WARMessage} object with given {@code type} and {@code payload}
     *
     * @param type    type of this {@code WARMessage} object
     * @param payload payload of this {@code WARMessage} object
     */
    public WARMessage(byte type, byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

    /**
     * Returns the human readable explanation of {@code type} values
     *
     * @param type type of {@code WARMessage} object
     * @return A {@code String} object that contains human readable explanation of given {@code WARMessage} {@code type}
     */
    private static String getTypeExplanation(byte type) {
        switch (type) {
            case 0:
                return "want game";
            case 1:
                return "game start";
            case 2:
                return "play card";
            case 3:
                return "play result";
            case 4:
                return "game result";
            case 5:
                return "matchmaking";
            case 6:
                return "correspondent connected";
            case 7:
                return "file hash";
            case 8:
                return "receive file with name";
            case 9:
                return "file transmit validation";
            default:
                return "invalid type";
        }
    }

    /**
     * Returns the {@code type} of this {@code WARMessage} object
     *
     * @return the {@code type} of this {@code WARMessage} object
     */
    public byte getType() {
        return type;
    }

    /**
     * Returns the {@code type} of this {@code WARMessage} object
     *
     * @return the {@code type} of this {@code WARMessage} object
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Returns the {@code String} representation of this {@code WARMessage} object
     *
     * @return the {@code String} representation of this {@code WARMessage} object
     */
    @Override
    public String toString() {
        return "WARMessage{" +
                "type=" + getTypeExplanation(type) +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }
}
