package service;

import contract.WARMessage;
import domain.Player;
import domain.WARGame;
import repository.MongoDBWARRepository;
import repository.WARRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class WarService {

    private static final WarService _instance = new WarService();
    private WARRepository warRepository;
    private List<WARGame> ongoingGames;
    private Map<Player, WARGame> playerToGameMap;

    private WarService() {
        warRepository = MongoDBWARRepository.getInstance();
        ongoingGames = new ArrayList<>();
        playerToGameMap = new HashMap<>();
    }

    public static synchronized WarService getInstance() {
        return _instance;
    }

    public void initializeGame(Player player1, Player player2) {
        WARGame newGame = new WARGame(player1, player2);
        ongoingGames.add(newGame);
        playerToGameMap.put(player1, newGame);
        playerToGameMap.put(player2, newGame);
    }

    public WARMessage handleWantGameMessage(WARMessage message, Player player) {
        return null;
    }

}
