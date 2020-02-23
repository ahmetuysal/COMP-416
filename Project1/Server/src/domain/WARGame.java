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
    private int numRounds;

    /**
     * Initializes a WARGame from scratch with given player parameters.
     *
     * @param player1 The first player of the game.
     * @param player2 The second player of the game.
     */
    public WARGame(Player player1, Player player2) {
        this.createdOn = new Date();
        this.lastChangedOn = new Date();
        this.player1 = player1;
        this.player2 = player2;
        initializeCards();
        isGameStarted = false;
        this.gameID = new ObjectId();
        this.numRounds = 0;
    }

    /**
     * Initializes an empty WARGame to be later initialized with values loaded from a document.
     *
     */
    public WARGame() {

    }

    /**
     * Initializes the card decks of each player randomly from a deck of 52 cards.
     * The resulting decks are provided to each player as a ByteList to later transmit the card values as bytes.
     *
     */
    private void initializeCards() {
        byte[] cardDeck = CARD_DECK.clone();
        Utilities.shuffleByteArray(cardDeck);
        byte[] player1Deck = Arrays.copyOfRange(cardDeck, 0, 26);
        byte[] player2Deck = Arrays.copyOfRange(cardDeck, 27, 52);
        this.player1.setCards(Utilities.byteArrayToByteList(player1Deck));
        this.player2.setCards(Utilities.byteArrayToByteList(player2Deck));
    }

    /**
     * Provides the creation time of the game
     *
     * @return createdOn field of the game, indicating the creation time.
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * Returns the last modification time of the game.
     *
     * @return
     */
    public Date getLastChangedOn() {
        return lastChangedOn;
    }

    /**
     * Sets the last modification time of the game.
     *
     * @param lastChangedOn The value to set the lastChangedOn field to.
     */
    public void setLastChangedOn(Date lastChangedOn) {
        this.lastChangedOn = lastChangedOn;
    }

    /**
     * Returns the first player of the game.
     *
     * @return player1 of the game.
     */
    public Player getPlayer1() {
        return player1;
    }

    /**
     * Returns the second player in the game.
     *
     * @return player2 of the game.
     */
    public Player getPlayer2() {
        return player2;
    }

    /**
     * Returns the opponent player in the game that corresponds to the given player.
     *
     * @param player The given player, the opponent of which is to be obtained.
     * @return the opponent of the given player.
     */
    public Player getOtherPlayer(Player player) {
        if (player1.equals(player)) {
            return player2;
        } else if (player2.equals(player)) {
            return player1;
        } else {
            return null;
        }
    }

    /**
     * Returns the started/ended state of the game.
     *
     * @return isGameStarted field that indicates whether the game has started or not.
     */
    public boolean isGameStarted() {
        return isGameStarted;
    }

    /**
     * Sets the gameStarted field to the given value (true/false).
     * Also initiates the lastChangedOn field, since starting or ending a game is a state change.
     *
     * @param gameStarted field to set the game state.
     */
    public void setGameStarted(boolean gameStarted) {
        isGameStarted = gameStarted;
        lastChangedOn = new Date();
    }

    /**
     * Returns the object ID of the game.
     *
     * @return the object ID of the game.
     */
    public ObjectId getGameID() {
        return this.gameID;
    }

    /**
     * Generates a document to be inserted into the database by appending the game ID, and the game object data directly.
     *
     * @return the document generated to insert into the database.
     */
    public Document generateWARDoc() {
        Document doc2gen = new Document()
                .append("_id", this.gameID)
                .append("game", this);
        return doc2gen;
    }

    /**
     * Loads the necessary game data that is obtained from the document given.
     * The game ID and game object are necessarily assigned to corresponding fields.
     *
     * @param doc Document to load data from.
     */
    public void loadFromDoc(Document doc) {
        this.gameID = doc.getObjectId("_id");
        this.createdOn = doc.getDate("created_on");
        this.lastChangedOn = doc.getDate("last_change");
        this.player1 = (Player) doc.get("player1");
        this.player2 = (Player) doc.get("player2");
        this.isGameStarted = doc.getBoolean("is_started");
        this.numRounds = doc.getInteger("rounds");
    }

    /**
     * Returns the number of rounds played.
     *
     * @return the number of rounds played in the game.
     */
    public int getNumRounds() {
        return numRounds;
    }

    /**
     * Sets the number of rounds played to the given value.
     *
     * @param rounds The value to set the number of rounds played to.
     */
    public void setNumRounds(int rounds) {
        this.numRounds = rounds;
    }

}
