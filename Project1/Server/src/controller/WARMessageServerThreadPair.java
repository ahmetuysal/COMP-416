package controller;

import contract.WARMessage;
import network.ServerThread;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class WARMessageServerThreadPair {
    private final WARMessage warMessage;
    private final ServerThread serverThread;

    public WARMessageServerThreadPair(WARMessage warMessage, ServerThread serverThread) {
        this.warMessage = warMessage;
        this.serverThread = serverThread;
    }

    public WARMessage getWarMessage() {
        return warMessage;
    }

    public ServerThread getServerThread() {
        return serverThread;
    }
}
