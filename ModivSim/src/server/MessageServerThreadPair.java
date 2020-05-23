package server;

import domain.Message;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class MessageServerThreadPair {
    private final Message message;
    private final ServerThread serverThread;


    public MessageServerThreadPair(Message message, ServerThread serverThread) {
        this.message = message;
        this.serverThread = serverThread;
    }

    /**
     * Returns the warMessage.
     *
     * @return warMessage field.
     */
    public Message getMessage() {
        return message;
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
