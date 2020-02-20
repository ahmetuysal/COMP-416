package controller;

import contract.WARMessage;
import domain.Player;
import service.WARService;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class ControllerThread extends Thread {

    private static final ControllerThread _instance = new ControllerThread();
    private LinkedBlockingQueue<WARMessagePlayerPair> waitingMessages;
    private WARService warService;

    private ControllerThread() {
        waitingMessages = new LinkedBlockingQueue<>();
        warService = WARService.getInstance();
    }

    public static ControllerThread getInstance() {
        return _instance;
    }

    public void queueIncomingWARMessage(WARMessage incomingMessage, Player source) {
        waitingMessages.add(new WARMessagePlayerPair(incomingMessage, source));
    }

    public void run() {
        while (true) {
            if (!waitingMessages.isEmpty()) {
                WARMessagePlayerPair messagePlayerPair = waitingMessages.poll();
                handleWARMessage(messagePlayerPair.getWarMessage(), messagePlayerPair.getPlayer());
            }
        }
    }

    private void handleWARMessage(WARMessage warMessage, Player player) {
        if (validateWarMessage(warMessage)) {
            // TODO: implement WARMessage handling
            if (warMessage.getType() == 0) {
                warService.handleWantGameMessage(warMessage, player);
            } else if (warMessage.getType() == 2) {
                warService.handlePlayCardMessage(warMessage, player);
            }
        } else {
            // TODO: send error message
        }
    }


    private boolean validateWarMessage(WARMessage warMessage) {
        // check payload based on message type
        switch (warMessage.getType()) {
            // want game
            case 0:
                return warMessage.getPayload() != null && !new String(warMessage.getPayload()).isEmpty();
            // play card
            case 2:
                // play result
            case 3:
                // game result
            case 4:
                if (warMessage.getPayload() == null || warMessage.getPayload().length != 1) {
                    return false;
                }
                break;
            // game start
            case 1:
                if (warMessage.getPayload() == null || warMessage.getPayload().length != 26) {
                    return false;
                }
                break;
            // matchmaking
            case 5:
                return warMessage.getPayload() == null || warMessage.getPayload().length == 0;
            // invalid WARMessage type
            default:
                return false;
        }

        // check card values
        // should only run on card related messages
        for (Byte card : warMessage.getPayload()) {
            if (card < 0 || card > 51) {
                return false;
            }
        }
        return true;
    }

}
