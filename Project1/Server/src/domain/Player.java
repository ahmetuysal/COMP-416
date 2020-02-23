package domain;

import java.util.List;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class Player implements Correspondent {
    private String name;
    private List<Byte> cards;
    private int point;
    private byte waitingPlayedCard;

    public Player() {
        waitingPlayedCard = -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Byte> getCards() {
        return cards;
    }

    public void setCards(List<Byte> cards) {
        this.cards = cards;
    }

    public boolean removeCard(byte card) {
        return cards.remove((Byte) card);
    }

    public int getPoint() {
        return point;
    }

    public void incrementPoint() {
        this.point++;
    }

    public byte getWaitingPlayedCard() {
        return waitingPlayedCard;
    }

    public void setWaitingPlayedCard(byte waitingPlayedCard) {
        this.waitingPlayedCard = waitingPlayedCard;
    }
}
