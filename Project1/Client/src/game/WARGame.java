package game;

import configuration.Configuration;
import connection.ConnectionToServer;
import contract.WARMessage;

import java.util.Arrays;
import java.util.Scanner;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class WARGame {

    private ConnectionToServer connectionToServer;

    public WARGame() {
        connectionToServer = new ConnectionToServer(Configuration.getInstance().getProperty("server.address"),
                Integer.parseInt(Configuration.getInstance().getProperty("server.port")));
        byte[] cards = connectToGame();
        System.out.println("Cards: " + Arrays.toString(cards));
    }

    private byte[] connectToGame() {
        WARMessage matchmakingMessage = connectionToServer.waitForAnswer();
        if (matchmakingMessage.getType() == 5) {
            Scanner sc = new Scanner(System.in);
            System.out.println("You are connected to game and matchmaking is done. Please enter your name to start the game: ");
            String name = sc.nextLine();
            WARMessage wantGameMessage = new WARMessage((byte) 0, name.getBytes());
            WARMessage gameStartMessage = connectionToServer.sendForAnswer(wantGameMessage);
            return gameStartMessage.getPayload();
        } else {
            System.out.println("Matchmaking failed, server didn't send the cards.");
            return null;
        }
    }

}
