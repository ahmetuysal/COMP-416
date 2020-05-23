package server;

import domain.Message;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;


public class ControllerThread extends Thread {

    private static final ControllerThread _instance = new ControllerThread();
    private Date lastUpdatedOn;
    private LinkedBlockingQueue<MessageServerThreadPair> waitingMessages;

    private ControllerThread() {
        waitingMessages = new LinkedBlockingQueue<>();
    }


    public static ControllerThread getInstance() {
        return _instance;
    }


    public void queueIncomingMessage(Message incomingMessage, ServerThread serverThread) {
        waitingMessages.add(new MessageServerThreadPair(incomingMessage, serverThread));
    }


    public void run() {
        while (true) {
            if (!waitingMessages.isEmpty()) {
                MessageServerThreadPair messageServerThreadPair = waitingMessages.poll();
                handleMessage(messageServerThreadPair.getMessage(), messageServerThreadPair.getServerThread());
            }
        }
    }

    private void handleMessage(Message  message, ServerThread serverThread) {

        // TODO: implement Message handling

    }


}
