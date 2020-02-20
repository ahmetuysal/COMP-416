package controller;

import contract.WARMessage;
import domain.Player;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class WARMessagePlayerPair {
    private final WARMessage warMessage;
    private final Player player;

    public WARMessagePlayerPair(WARMessage warMessage, Player player) {
        this.warMessage = warMessage;
        this.player = player;
    }

    public WARMessage getWarMessage() {
        return warMessage;
    }

    public Player getPlayer() {
        return player;
    }
}
