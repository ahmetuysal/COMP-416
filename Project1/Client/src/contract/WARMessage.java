package contract;

import java.io.Serializable;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
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
}
