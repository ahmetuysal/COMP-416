package game;

import configuration.Configuration;
import connection.ConnectionToServer;
import contract.WARMessage;
import util.Utilities;

import java.util.List;
import java.util.Scanner;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class WARGame {

    private List<Byte> cards;
    private ConnectionToServer connectionToServer;

    public WARGame() {
        connectionToServer = new ConnectionToServer(Configuration.getInstance().getProperty("server.address"),
                Integer.parseInt(Configuration.getInstance().getProperty("server.port")));
        this.cards = Utilities.byteArrayToByteList(connectToGame());
        while (!cards.isEmpty()) {
            playTurn();
        }
    }

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

    private void playTurn() {
        System.out.println("Cards: " + cards.toString());
        Scanner sc = new Scanner(System.in);
        System.out.print("Select which card to play: ");
        byte card = sc.nextByte();
        while (!cards.contains(card)) {
            System.out.print("Select a valid card: ");
            card = sc.nextByte();
        }

        cards.remove((Byte) card);

        WARMessage playCardMessage = new WARMessage((byte) 2, new byte[]{card});
        WARMessage playResultMessage = connectionToServer.sendForAnswer(playCardMessage);
        System.out.println("Play result: " + playResultMessage.toString());
    }

}
