package controller;

import contract.WARMessage;
import domain.Player;
import repository.WARRepository;
import service.WARService;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class ControllerThread extends Thread {

    private static final ControllerThread _instance = new ControllerThread();
    private Date lastUpdatedOn;
    private LinkedBlockingQueue<WARMessagePlayerPair> waitingMessages;
    private WARService warService;

    private ControllerThread() {
        waitingMessages = new LinkedBlockingQueue<>();
        warService = WARService.getInstance();
        lastUpdatedOn = new Date();
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
            Date currentTime = new Date();
            if (currentTime.getTime() - lastUpdatedOn.getTime() >= 30000) {
                warService.getOngoingGames().stream()
                        .forEach(
                                warGame -> {
                                if (warGame.getLastChangedOn().getTime() > lastUpdatedOn.getTime()) {
                                    System.out.println("Current time: " + currentTime.toString() +  ", the following files are going to be synchronized:\n");
                                    warService.updateGame(warGame);
                                    // backup to the follower here as well?
                                    warGame.setLastChangedOn(currentTime);
                                } else {
                                    System.out.println("“Current time: " + currentTime.toString() + ", no update is needed. Already synced!”");
                                }
                        });
                lastUpdatedOn = currentTime;
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
            // follower connected
            case 6:
            // follower answered
            case 7:
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
