package repository;

import controller.WARData;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public interface WARRepository {


    public String insertGame(WARData gameData);
    public WARData retrieveGame(String objID);
    public void updateGame( /* need params */ );
    public void deleteGame( /* need params */ );



}
