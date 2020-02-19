package controller;

import contract.WARMessage;
import domain.Player;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class ControllerThread extends Thread {

    private static final ControllerThread _instance = new ControllerThread();

    private ControllerThread() {}

    public static ControllerThread getInstance() {
        return _instance;
    }

    private LinkedBlockingQueue<WARMessage> waitingMessages;

    public void queueIncomingWARMessage(WARMessage incomingMessage, Player source) {
        waitingMessages.add(incomingMessage);
    }

    public void run() {
        while (true) {
            if (!waitingMessages.isEmpty()) {
                WARMessage message = waitingMessages.poll();
                handleWARMessage(message);
            }
        }
    }

    private WARMessage handleWARMessage(WARMessage warMessage) {
        if (validateWarMessage(warMessage)) {
            // TODO: implement WARMessage handling
            if (warMessage.getType() == 0) {

            }
        } else {
            // TODO: send error message
        }
        return warMessage;
    }


    private boolean validateWarMessage(WARMessage warMessage) {
        // check payload based on message type
        switch (warMessage.getType()) {
            // want game
            case 0:
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
        for (Byte card : warMessage.getPayload()) {
            if (card < 0 || card > 51) {
                return false;
            }
        }
        return true;
    }

}
