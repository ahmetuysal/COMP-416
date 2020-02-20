package service;

import contract.WARMessage;
import domain.Player;
import domain.WARGame;
import network.ServerThread;
import repository.MongoDBWARRepository;
import repository.WARRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class WARService {

    private static final WARService _instance = new WARService();
    private WARRepository warRepository;
    private List<WARGame> ongoingGames;
    private Map<Player, WARGame> playerToGameMap;
    private Map<Player, ServerThread> playerToServerThreadMap;

    private WARService() {
        warRepository = MongoDBWARRepository.getInstance();
        ongoingGames = new ArrayList<>();
        playerToGameMap = new HashMap<>();
        playerToServerThreadMap = new HashMap<>();
    }

    public static synchronized WARService getInstance() {
        return _instance;
    }

    public void initializeGame(Player player1, Player player2) {
        WARGame newGame = new WARGame(player1, player2);
        ongoingGames.add(newGame);
        playerToGameMap.put(player1, newGame);
        playerToGameMap.put(player2, newGame);
    }

    public void registerPlayer(Player player, ServerThread serverThread) {
        this.playerToServerThreadMap.put(player, serverThread);
    }

    public WARMessage handleWantGameMessage(WARMessage message, Player player) {
        player.setName(new String(message.getPayload()));
        WARGame game = playerToGameMap.get(player);
        return null;
    }

}
