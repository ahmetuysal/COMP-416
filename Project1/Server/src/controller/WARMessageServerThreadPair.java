package controller;

import domain.WARMessage;
import network.ServerThread;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class WARMessageServerThreadPair {
    private final WARMessage warMessage;
    private final ServerThread serverThread;

    /**
     *
     * Initializes WARMessageServerThreadPair, which pairs a message with a server thread.
     * @param warMessage the value to set WARMessage.
     * @param serverThread the value to set serverThread.
     */
    public WARMessageServerThreadPair(WARMessage warMessage, ServerThread serverThread) {
        this.warMessage = warMessage;
        this.serverThread = serverThread;
    }

    /**
     * Returns the warMessage.
     *
     * @return warMessage field.
     */
    public WARMessage getWarMessage() {
        return warMessage;
    }

    /**
     * Returns the serverThread.
     *
     * @return serverThread field.
     */
    public ServerThread getServerThread() {
        return serverThread;
    }
}
