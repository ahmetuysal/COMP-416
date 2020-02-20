package repository;

import controller.WARData;
import domain.WARGame;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public interface WARRepository {


    public void insertGame(WARGame gameData);
    public void retrieveGame(String objID);
    public void updateGame( /* need params */ );
    public void deleteGame( /* need params */ );



}
