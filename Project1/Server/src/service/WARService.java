package service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import contract.WARMessage;
import domain.Correspondent;
import domain.Follower;
import domain.Player;
import domain.WARGame;
import network.ServerThread;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import repository.MongoDBWARRepository;
import repository.WARRepository;
import util.Utilities;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

    private WARService() {
        warRepository = MongoDBWARRepository.getInstance();
        ongoingGames = new ArrayList<>();
        followers = new ArrayList<>();
        playerToGameMap = new HashMap<>();
        correspondentToServerThreadMap = new HashMap<>();
    }

    public static synchronized WARService getInstance() {
        return _instance;
    }

    private void initializeGame(Player player1, Player player2) {
        newGame = new WARGame(player1, player2);
        ongoingGames.add(newGame);
        warRepository.insertGame(newGame);
        playerToGameMap.put(player1, newGame);
        playerToGameMap.put(player2, newGame);
    }

    public synchronized void registerFollower(ServerThread serverThread) {
        Follower follower = new Follower();
        followers.add(follower);
        serverThread.setCorrespondent(follower);
        this.correspondentToServerThreadMap.put(follower, serverThread);
    }

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

    public void handleTermination(Correspondent correspondent) {
        if (correspondent instanceof Follower) {
            // TODO: handle follower termination
        } else if (correspondent instanceof  Player) {
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

    public void sendHashCodeToFollower(Correspondent correspondent){
//        file =new File(game.getPlayer1().getName() + "-" + game.getPlayer2().getName() + ".json");
//        ServerThread followerThread = correspondentToServerThreadMap.get((Follower) correspondent);
//        followerThread.sendWARMessage(new WARMessage((byte) 7, new byte[]{ (byte) file.hashCode()}));
    }

    public void handleFollowerUpdate(){
        followers.stream().forEach(follower -> {
            Date followerLastUpdateTime = follower.getLastUpdatedOn();
            ongoingGames.stream()
                    .filter(game-> game.getLastChangedOn().compareTo(followerLastUpdateTime) > 0)
                    .forEach(warGame -> {

                            }
                    );
        });

//        for(int i=0; i < followers.size(); i++){
//            WARGame game = followerToGameMap.get(followers.get(i));
//            if(game != null && game.getPlayer1() != null && game.getPlayer1() != null){
//                file =new File(game.getPlayer1().getName() + "-" + game.getPlayer2().getName() + ".json");
//                if(!followers.get(i).isUpdated() && file.length() != 0){
//                    System.out.println("JSON file transmit " + followers.get(i));
//                    ServerThread followerThread = correspondentToServerThreadMap.get(followers.get(i));
//                    //followerThread.sendFile(file);
//                    //sendHashCodeToFollower(followerThread, file);
//                    followers.get(i).setUpdated(true);
//                }
//            }
//        }
    }

    public List<WARGame> getOngoingGames() {
        return this.ongoingGames;
    }

    public void updateGame(WARGame game){
        warRepository.updateGame(game);
    }

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
