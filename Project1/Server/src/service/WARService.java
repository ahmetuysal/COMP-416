package service;

import domain.*;
import network.ServerThread;
import repository.MongoDBWARRepository;
import repository.WARRepository;
import util.Utilities;

import java.io.File;
import java.util.*;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class WARService {

    private static final WARService _instance = new WARService();
    private Player waitingPlayer = null;
    private WARRepository warRepository;
    private WARGame newGame;
    private List<WARGame> ongoingGames;
    private Map<Player, WARGame> playerToGameMap;
    private Map<Correspondent, ServerThread> correspondentToServerThreadMap;
    private List<Follower> followers;

    /**
     * Initializes the WARService object that interprets the WARMessages and implements the game logic,
     * while acting as an interface between additional components such as database, etc. and threads.
     *
     */
    private WARService() {
        warRepository = MongoDBWARRepository.getInstance();
        ongoingGames = new ArrayList<>();
        followers = new ArrayList<>();
        playerToGameMap = new HashMap<>();
        correspondentToServerThreadMap = new HashMap<>();
    }

    /**
     * Gets the {@code WARService} object that takes an active part in interpretation of WARMessages and in game
     * implementation.
     *
     * @return {@code WARMessage} to perform message interpretation and game based services.
     */
    public static synchronized WARService getInstance() {
        return _instance;
    }

    /**
     * Initializes a new {@code WARGame} object with the give players, and performs the corresponding list and map
     * placements of such objects.
     *
     * @param player1 Player 1 of the game to be initialized.
     * @param player2 Player 2 of the game to be initialized.
     */
    private void initializeGame(Player player1, Player player2) {
        newGame = new WARGame(player1, player2);
        ongoingGames.add(newGame);
        warRepository.insertGame(newGame);
        playerToGameMap.put(player1, newGame);
        playerToGameMap.put(player2, newGame);
    }

    /**
     * Generates a new follower to be added to the followers array, and perform the mapping with the
     * corresponding server thread (via the contained map).
     *
     * @param serverThread The server thread to map the newly generated follower to.
     */
    public synchronized void registerFollower(ServerThread serverThread) {
        Follower follower = new Follower();
        followers.add(follower);
        serverThread.setCorrespondent(follower);
        this.correspondentToServerThreadMap.put(follower, serverThread);
    }

    /**
     * Registers players and redirects them to the matchmaking process.
     * If the matchmaking is complete, a new game with the provided players is initiated.
     *
     * @param serverThread The server thread, the {@code Correspondent} of which is the newly initialized player.
     */
    public synchronized void registerPlayer(ServerThread serverThread) {
        Player player = new Player();
        serverThread.setCorrespondent(player);
        this.correspondentToServerThreadMap.put(player, serverThread);

        if (waitingPlayer == null) {
            waitingPlayer = player;
        } else {
            ServerThread opponentThread = correspondentToServerThreadMap.get(waitingPlayer);
            if (opponentThread.isSocketOpen()) {
                WARMessage matchmakingMessage = new WARMessage((byte) 5, null);
                opponentThread.sendWARMessage(matchmakingMessage);
                serverThread.sendWARMessage(matchmakingMessage);
                initializeGame(waitingPlayer, player);
                waitingPlayer = null;
            } else {
                waitingPlayer = player;
            }
        }

    }

    /**
     * Compares the played card values to determine the outcome of the current round.
     * Sets the number of rounds played.
     * Checks whether the game has ended to output the game result, and acts accordingly.
     *
     * @param message WARMessage received by the player that contains the card value to compare.
     * @param correspondent {@code Correspondent} to be cast into a {@code Player} object that sent the above message.
     */
    public void handlePlayCardMessage(WARMessage message, Correspondent correspondent) {
        if (!(correspondent instanceof Player)) {
            System.out.println("A correspondent with non-player type tried to send play card message");
            return;
        }

        Player player = (Player) correspondent;

        System.out.println("Handling play card message: " + message.toString());
        // TODO: validate game & threads exist
        WARGame game = playerToGameMap.get(player);
        // TODO: validate game has started
        Player otherPlayer = game.getOtherPlayer(player);
        byte playerCard = message.getPayload()[0];

        // Validate player has that card
        if (!player.removeCard(playerCard)) {
            // TODO: send an error message
            return;
        }

        if (otherPlayer.getWaitingPlayedCard() != -1) {
            // other player already sent play card message
            byte opponentCard = otherPlayer.getWaitingPlayedCard();
            // remove waiting played card
            otherPlayer.setWaitingPlayedCard((byte) -1);
            WARMessage playerPlayResultMessage;
            WARMessage opponentPlayResultMessage;
            System.out.println(playerCard + " " + opponentCard);
            if (playerCard % 13 > opponentCard % 13) {
                player.incrementPoint();
                playerPlayResultMessage = new WARMessage((byte) 3, new byte[]{0});
                opponentPlayResultMessage = new WARMessage((byte) 3, new byte[]{2});
            } else if (opponentCard % 13 > playerCard % 13) {
                otherPlayer.incrementPoint();
                playerPlayResultMessage = new WARMessage((byte) 3, new byte[]{2});
                opponentPlayResultMessage = new WARMessage((byte) 3, new byte[]{0});
            } else {
                playerPlayResultMessage = new WARMessage((byte) 3, new byte[]{1});
                opponentPlayResultMessage = new WARMessage((byte) 3, new byte[]{1});
            }

            ServerThread playerThread = correspondentToServerThreadMap.get(player);
            ServerThread opponentThread = correspondentToServerThreadMap.get(otherPlayer);
            playerThread.sendWARMessage(playerPlayResultMessage);
            opponentThread.sendWARMessage(opponentPlayResultMessage);

            game.setNumRounds(game.getNumRounds() + 1);
            System.out.println("Rounds played: " + game.getNumRounds());

            // Check whether game has ended
            if (player.getCards().isEmpty()) {
                WARMessage playerGameResultMessage;
                WARMessage opponentGameResultMessage;
                int playerScore = player.getPoint();
                int opponentScore = otherPlayer.getPoint();
                if (playerScore > opponentScore) {
                    playerGameResultMessage = new WARMessage((byte) 4, new byte[]{0});
                    opponentGameResultMessage = new WARMessage((byte) 4, new byte[]{2});
                } else if (opponentScore > playerScore) {
                    playerGameResultMessage = new WARMessage((byte) 4, new byte[]{2});
                    opponentGameResultMessage = new WARMessage((byte) 4, new byte[]{0});
                } else {
                    playerGameResultMessage = new WARMessage((byte) 4, new byte[]{1});
                    opponentGameResultMessage = new WARMessage((byte) 4, new byte[]{1});
                }
                playerThread.sendWARMessage(playerGameResultMessage);
                opponentThread.sendWARMessage(opponentGameResultMessage);
                terminateGame(game);
            }

        } else {
            // other player didn't send a play card message yet
            player.setWaitingPlayedCard(message.getPayload()[0]);
        }

        game.setLastChangedOn(new Date());
    }

    /**
     * Assigns the name of the player, and then sends the corresponding card decks while starting the game
     * if both names have been provided.
     * Utilizes both the given player and its opponent while performing the above mentioned game starting logic.
     *
     * @param message {@code WARMessage} that contains the player name.
     * @param correspondent {@code Correspondent} to be cast into a {@code Player} object that sent the above message.
     */
    public void handleWantGameMessage(WARMessage message, Correspondent correspondent) {
        if (!(correspondent instanceof Player)) {
            System.out.println("A correspondent with non-player type tried to send want game message");
            return;
        }

        Player player = (Player) correspondent;

        System.out.println("Handling want game message: " + message.toString());
        // TODO: validate game & threads exist
        player.setName(new String(message.getPayload()));
        WARGame game = playerToGameMap.get(player);
        Player otherPlayer = game.getOtherPlayer(player);
        if (otherPlayer.getName() != null && !otherPlayer.getName().isEmpty()) {
            game.setGameStarted(true);
            ServerThread player1Thread = correspondentToServerThreadMap.get(player);
            ServerThread player2Thread = correspondentToServerThreadMap.get(otherPlayer);
            WARMessage player1GameStartMessage = new WARMessage((byte) 1, Utilities.byteListToByteArray(player.getCards()));
            WARMessage player2GameStartMessage = new WARMessage((byte) 1, Utilities.byteListToByteArray(otherPlayer.getCards()));
            player1Thread.sendWARMessage(player1GameStartMessage);
            player2Thread.sendWARMessage(player2GameStartMessage);
        }
    }

    /**
     * Terminates the game mapped to the provided {@code Correspondent}, i.e. the {@code Player} that caused the
     * termination of the game. Also sends a message to the opponent of the player provided, indicating that it
     * has won the game, and therefore terminates its game as well.
     *
     * @param correspondent {@code Correspondent} to be cast into a {@code Player} object that terminated the game.
     */
    public void handleTermination(Correspondent correspondent) {
        if (correspondent instanceof Follower) {
            // TODO: handle follower termination
        } else if (correspondent instanceof Player) {
            // TODO: handle player termination
            Player player = (Player) correspondent;
            if (!playerToGameMap.containsKey(player)) {
                return;
            }
            WARGame terminatedGame = playerToGameMap.get(player);
            Player opponent = terminatedGame.getOtherPlayer(player);
            WARMessage opponentGameResultMessage = new WARMessage((byte) 4, new byte[]{0});
            ServerThread opponentThread = correspondentToServerThreadMap.get(opponent);
            opponentThread.sendWARMessage(opponentGameResultMessage);
            terminateGame(terminatedGame);
        }

    }

    /**
     * Updates the followers if there has been any change in the files.
     *
     */
    public void handleFollowerUpdate() {
        followers.parallelStream().forEach(follower -> {
            Date followerLastUpdateTime = follower.getLastUpdatedOn();
            ongoingGames.stream()
                    .filter(game -> game.getLastChangedOn().compareTo(followerLastUpdateTime) > 0)
                    .forEach(warGame -> {
                        // TODO: extract to method and send again if fails
                        ServerThread followerThread = correspondentToServerThreadMap.get(follower);
                        sendWARGameFileToFollower(warGame.getPlayer1().getName() + "-" + warGame.getPlayer2().getName() + ".json", followerThread);
                            }
                    );
            follower.setLastUpdatedOn(new Date());
        });

    }

    /**
     * Sends the name of the file and the file itself, respectively, to the follower that will save it.
     * Hash values of the file content are later sent to the follower for validation.
     *
     * @param fileName Name of the file to be sent.
     * @param followerThread The thread of {@code Follower} that saves the file.
     */
    private void sendWARGameFileToFollower(String fileName, ServerThread followerThread){
        WARMessage fileNameMessage = new WARMessage((byte) 8, fileName.getBytes());
        followerThread.sendWARMessage(fileNameMessage);
        File warGameFile = new File(fileName);
        followerThread.sendFile(warGameFile);

        byte[] fileHash = Utilities.calculateFileChecksum(warGameFile);
        followerThread.sendWARMessage(new WARMessage((byte) 7, fileHash));

    }


    /**
     * First checks if the message is a file transmit validation message, then performs the consistency check.
     * The outcome is printed.
     *
     * @param validationMessage {@code WARMessage} to check the type of.
     * @param followerThread The thread of {@code Follower} to send the file to revalidate.
     */
    public void fileTransferValidation(WARMessage validationMessage, ServerThread followerThread){
        if(validationMessage.getType() == 9){
            String message = new String(validationMessage.getPayload());
            int indexOfSpace = message.indexOf(" ");

            if(message.startsWith("CONSISTENCY_CHECK_PASSED"))
                return;
            else if(message.startsWith("RETRANSMIT"))
                sendWARGameFileToFollower(message.substring(indexOfSpace+1), followerThread);
        }
    }

    /**
     * Gets the list of the ongoing WARGames
     *
     * @return list of WARGames that have not been terminated.
     */
    public List<WARGame> getOngoingGames() {
        return this.ongoingGames;
    }

    /**
     * Updates the game data in the database with the provided game data.
     *
     * @param game Newly provided {@code WARGame} data to update the game data contained in the database with.
     */
    public void updateGame(WARGame game) {
        warRepository.updateGame(game);
    }

    /**
     * Completely terminates the provided game, by also removing the players contained within the game and their
     * corresponding threads.
     * Previously generated JSON files and the documents contained in the database are also deleted.
     *
     * @param game {@code WARGame} to be terminated.
     */
    private void terminateGame(WARGame game) {
        Player player1 = game.getPlayer1();
        Player player2 = game.getPlayer2();
        playerToGameMap.remove(player1);
        playerToGameMap.remove(player2);
        ongoingGames.remove(game);
        Utilities.deleteWarGameJSONFile(game);
        ServerThread player1Thread = correspondentToServerThreadMap.remove(player1);
        ServerThread player2Thread = correspondentToServerThreadMap.remove(player2);
        player1Thread.terminate();
        player2Thread.terminate();
        warRepository.deleteGame(game);
    }

}
