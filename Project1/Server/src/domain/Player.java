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

    /**
     * Initializes Player, a Correspondent of Master Server
     */
    public Player() {
        waitingPlayedCard = -1;
    }

    /**
     * Returns the name of the player.
     *
     * @return name field, name of the player.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the player.
     *
     * @param name The value to set name field.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the cards of the player.
     *
     * @return cards field, cards of the player.
     */
    public List<Byte> getCards() {
        return cards;
    }

    /**
     * Sets the cards of the player.
     *
     * @param cards The value to set cards field.
     */
    public void setCards(List<Byte> cards) {
        this.cards = cards;
    }

    /**
     * Removes a card from the cards of the player.
     *
     * @param card The value to remove from the cards field.
     * @return true if card successfully removed from the cards, false if card could not be removed from the cards.
     */
    public boolean removeCard(byte card) {
        return cards.remove((Byte) card);
    }

    /**
     * Returns the point of the player.
     *
     * @return point field, the point the player gains in total.
     */
    public int getPoint() {
        return point;
    }

    /**
     * Increases the point field 1.
     */
    public void incrementPoint() {
        this.point++;
    }

    /**
     * Returns the waiting played card of the player.
     *
     * @return waitingPlayedCard field, the played card of the player if it is waiting for the other player to play.
     */
    public byte getWaitingPlayedCard() {
        return waitingPlayedCard;
    }

    /**
     * Sets the waiting played card of the player.
     *
     * @param waitingPlayedCard The value to set waitingPlayedCard field.
     */
    public void setWaitingPlayedCard(byte waitingPlayedCard) {
        this.waitingPlayedCard = waitingPlayedCard;
    }
}
