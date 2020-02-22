package controller;

import contract.WARMessage;
import domain.Correspondent;
import domain.Follower;
import domain.Player;
import network.ServerThread;
import service.WARService;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class ControllerThread extends Thread {

    private static final ControllerThread _instance = new ControllerThread();
    private Date lastUpdatedOn;
    private LinkedBlockingQueue<WARMessageServerThreadPair> waitingMessages;
    private WARService warService;

    private ControllerThread() {
        waitingMessages = new LinkedBlockingQueue<>();
        warService = WARService.getInstance();
        lastUpdatedOn = new Date();
    }

    public static ControllerThread getInstance() {
        return _instance;
    }

    public void queueIncomingWARMessage(WARMessage incomingMessage, ServerThread serverThread) {
        waitingMessages.add(new WARMessageServerThreadPair(incomingMessage, serverThread));
    }

    public void run() {
        while (true) {
            if (!waitingMessages.isEmpty()) {
                WARMessageServerThreadPair messageServerThreadPair = waitingMessages.poll();
                handleWARMessage(messageServerThreadPair.getWarMessage(), messageServerThreadPair.getServerThread());
            }
            handleFollowerUpdate();
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

    private void handleWARMessage(WARMessage warMessage, ServerThread serverThread) {
        if (validateWarMessage(warMessage)) {
            // TODO: implement WARMessage handling
            if (warMessage.getType() == 0) {
                warService.handleWantGameMessage(warMessage, serverThread.getCorrespondent());
            } else if (warMessage.getType() == 2) {
                warService.handlePlayCardMessage(warMessage, serverThread.getCorrespondent());
            }
            else if (warMessage.getType() == 6) {
                byte correspondentType = warMessage.getPayload()[0];
                // player connected
                if (correspondentType == (byte) 0) {
                    warService.registerPlayer(serverThread);
                }
                // follower connected
                else if (correspondentType == (byte) 1) {
                    warService.registerFollower(serverThread);
                }
            }else if (warMessage.getType() == 8) {
                warService.sendHashCodeToFollower(serverThread.getCorrespondent());
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
            // correspondent connected
            case 6:
                return warMessage.getPayload() != null && warMessage.getPayload().length == 1;
            // follower communication
            case 7:
                return true;
            // ask hashcode
            case 8:
                return true;
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

    private void handleFollowerUpdate(){
        warService.handleFollowerUpdate();
    }

}
