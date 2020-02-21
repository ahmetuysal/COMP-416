package contract;

import java.io.Serializable;
import java.util.Arrays;

public class WARMessage implements Serializable {
    private static final long serialVersionUID = -5817893373429527297L;
    private final byte type;
    private final byte[] payload;

    public WARMessage(byte type, byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

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
                return "follower connected";
            case 7:
                return "follower answered";
            default:
                return "invalid type";
        }
    }

    public byte getType() {
        return type;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "WARMessage{" +
                "type=" + getTypeExplanation(type) +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }
}
