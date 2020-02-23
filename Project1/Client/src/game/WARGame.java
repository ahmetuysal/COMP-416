package game;

import configuration.Configuration;
import connection.ConnectionToServer;
import domain.WARMessage;
import util.Utilities;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Domain object that is responsible for implementing game rules and flow.
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class WARGame {

    private List<Byte> cards;
    private ConnectionToServer connectionToServer;

    /**
     * Creates a new instance of {@code WARGame}
     */
    public WARGame() {
        startNewGame();
    }

    /**
     * Opens a connection to master server, notifies this connection is for a player, and carries out the matchmaking
     * related communication. Continuously calls {@link #playTurn()} method until the game end
     */
    private void startNewGame() {
        connectionToServer = new ConnectionToServer(Configuration.getInstance().getProperty("server.address"),
                Integer.parseInt(Configuration.getInstance().getProperty("server.port")));
        this.cards = Utilities.byteArrayToByteList(connectToGame());
        while (!cards.isEmpty() && connectionToServer.isConnectionActive()) {
            playTurn();
        }
    }

    /**
     * Notifies this connection is for a player, and carries out the matchmaking related communication.
     * @return {@code byte} array that contains cards
     */
    private byte[] connectToGame() {
        WARMessage iAmPlayerMessage = new WARMessage((byte) 6, new byte[]{0});
        connectionToServer.send(iAmPlayerMessage);
        WARMessage matchmakingMessage = connectionToServer.waitForAnswer();
        if (matchmakingMessage.getType() == 5) {
            Scanner sc = new Scanner(System.in);
            System.out.print("You are connected to game and matchmaking is done. Please enter your name to start the game: ");
            String name = sc.nextLine();
            WARMessage wantGameMessage = new WARMessage((byte) 0, name.getBytes());
            WARMessage gameStartMessage = connectionToServer.sendForAnswer(wantGameMessage);
            return gameStartMessage.getPayload();
        } else {
            System.out.println("Matchmaking failed, server didn't send the cards.");
            return null;
        }
    }

    /**
     * Implements logic of one WARGame turn by handling required console I/O operations and WARGame transmissions
     */
    private void playTurn() {
        System.out.println("Cards: " + cards.toString());
        Scanner sc = new Scanner(System.in);
        System.out.print("What would you like to do?"
                + "\n1: Play card."
                + "\n2: Start a new game."
                + "\n3: Quit game."
                + "\nChoice: ");
        byte opt = sc.nextByte();

        if (!connectionToServer.isConnectionActive())
            return;

        if (opt == 1) {
            Random rand_ind = new Random();
            Byte card = cards.get(rand_ind.nextInt(cards.size()));
            /*
            while (!cards.contains(card)) {
                System.out.print("Select a valid card: ");
                card = sc.nextByte();
            }
            */
            cards.remove(card);
            WARMessage playCardMessage = new WARMessage((byte) 2, new byte[]{card});
            WARMessage playResultMessage = connectionToServer.sendForAnswer(playCardMessage);
            if (playResultMessage == null) {
                System.out.println("You won the game");
            } else {
                handleWarMessage(playResultMessage);
            }
        } else if (opt == 2) {
            connectionToServer.disconnect();
            startNewGame();
        } else if (opt == 3) {
            this.cards.clear();
            connectionToServer.disconnect();
        } else {
            System.out.println("Incorrect input!");
        }
    }

    /**
     * Interprets given {@param warMessage} object based on game rules
     * @param warMessage {@code WARMessage} object to handle
     */
    private void handleWarMessage (WARMessage warMessage) {
        switch (warMessage.getType()) {
            case 3:
                if (warMessage.getPayload()[0] == 0) {
                    System.out.println("You won this round");
                } else if (warMessage.getPayload()[0] == 1) {
                    System.out.println("This round is tied");
                } else if (warMessage.getPayload()[0] == 2) {
                    System.out.println("You lost this round");
                } else {
                    System.out.println("Invalid turn result message!");
                }
                return;
            case 4:
                if (warMessage.getPayload()[0] == 0) {
                    System.out.println("You won the game");
                } else if (warMessage.getPayload()[0] == 1) {
                    System.out.println("The game is tied");
                } else if (warMessage.getPayload()[0] == 2) {
                    System.out.println("You lost the game");
                } else {
                    System.out.println("Invalid game result message!");
                }
                connectionToServer.disconnect();
                return;
            default:
                System.out.println("Something is wrong");
        }
    }

}
