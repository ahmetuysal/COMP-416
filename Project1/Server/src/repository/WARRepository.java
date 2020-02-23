package repository;

import domain.WARGame;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public interface WARRepository {
    /**
     * Inserts the give gameData to the database.
     *
     * @param gameData Game data to be inserted into the database.
     */
    void insertGame(WARGame gameData);

    /**
     * Obtains the game data with the given ID from the database.
     *
     * @param objID ID of the game to be retrieved from the database.
     */
    void retrieveGame(String objID);

    /**
     * Updates the given gameData in the database.
     *
     * @param gameData Game data to be updated in the database.
     */
    void updateGame(WARGame gameData);

    /**
     * Deletes the given game data from the server.
     *
     * @param gameData Game data to be deleted from the database.
     */
    void deleteGame(WARGame gameData);
}
