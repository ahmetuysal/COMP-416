package domain;

import java.util.Map;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class Message {

    private final int senderId;
    private final int receiverId;
    private final int linkBandwidth;
    private final Map<Integer, Integer> senderDistanceVector;

    public Message(int senderId, int receiverId, int linkBandwidth, Map<Integer, Integer> senderDistanceVector) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.linkBandwidth = linkBandwidth;
        this.senderDistanceVector = senderDistanceVector;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public int getLinkBandwidth() {
        return linkBandwidth;
    }

    public Map<Integer, Integer> getSenderDistanceVector() {
        return senderDistanceVector;
    }
}
