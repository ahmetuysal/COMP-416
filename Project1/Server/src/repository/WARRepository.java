package repository;

import domain.WARGame;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public interface WARRepository {
    void insertGame(WARGame gameData);
    void retrieveGame(String objID);
    void updateGame(WARGame gameData);
    void deleteGame(WARGame gameData);
}
