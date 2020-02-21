package domain;

import org.bson.Document;
import org.bson.types.ObjectId;
import util.Utilities;

import java.util.Arrays;
import java.util.Date;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class WARGame {

    private static final byte[] CARD_DECK = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
            19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45,
            46, 47, 48, 49, 50, 51};

    private Date createdOn;
    private Date lastChangedOn;
    private Player player1;
    private Player player2;
    private boolean isGameStarted;
    private ObjectId gameID;

    public WARGame(Player player1, Player player2) {
        this.createdOn = new Date();
        this.lastChangedOn = new Date();
        this.player1 = player1;
        this.player2 = player2;
        initializeCards();
        isGameStarted = false;
        this.gameID = new ObjectId();
    }

    public WARGame() {

    }

    private void initializeCards() {
        byte[] cardDeck = CARD_DECK.clone();
        Utilities.shuffleByteArray(cardDeck);
        byte[] player1Deck = Arrays.copyOfRange(cardDeck, 0, 26);
        byte[] player2Deck = Arrays.copyOfRange(cardDeck, 27, 52);
        this.player1.setCards(Utilities.byteArrayToByteList(player1Deck));
        this.player2.setCards(Utilities.byteArrayToByteList(player2Deck));
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public Date getLastChangedOn() {
        return lastChangedOn;
    }

    public void setLastChangedOn(Date lastChangedOn) {
        this.lastChangedOn = lastChangedOn;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Player getOtherPlayer(Player player) {
        if (player1.equals(player)) {
            return player2;
        } else if (player2.equals(player)) {
            return player1;
        } else {
            return null;
        }
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        isGameStarted = gameStarted;
        lastChangedOn = new Date();
    }

    public ObjectId getGameID() {
        return this.gameID;
    }

    public Document generateWARDoc() {
        Document doc2gen = new Document()
                .append("_id", this.gameID)
                .append("player1", this.player1)
                .append("player2", this.player2)
                .append("created_on", this.createdOn)
                .append("last_change", this.lastChangedOn)
                .append("is_started", this.isGameStarted);
        return doc2gen;
    }

    public void loadFromDoc(Document doc) {
        this.gameID =  doc.getObjectId("_id");
        this.createdOn = doc.getDate("created_on");
        this.lastChangedOn = doc.getDate("last_change");
        this.player1 =  (Player) doc.get("player1");
        this.player2 =  (Player) doc.get("player2");
        this.isGameStarted = doc.getBoolean("is_started");
    }
}