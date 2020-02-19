package repository;

import controller.WARData;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public interface WARRepository {


    public void insertGame(WARData gameData);
    public void retrieveGame( /* need params */ );
    public void updateGame( /* need params */ );
    public void deleteGame( /* need params */ );



}
