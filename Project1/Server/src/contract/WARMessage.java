package contract;

import java.io.Serializable;
import java.util.Arrays;

public class WARMessage implements Serializable {
    private static final long serialVersionUID = -5817893373429527297L;
    private byte type;
    private byte[] payload;

    public WARMessage(byte type, byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "WARMessage{" +
                "type=" + getTypeExplanation(type) +
                ", payload=" + Arrays.toString(payload) +
                '}';
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
            default:
                return "invalid type";
        }
    }
}
