package main;

import domain.Message;
import domain.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

/**
 * @author Ahmet Uysal @ahmetuysal
 */
public class ModivSim {

    public static final LinkedBlockingQueue<Message> concurrentMessageQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {

        HashMap<Integer, Node> nodes = new HashMap<>();

        try (Stream<Path> walk = Files.walk(Paths.get("src/nodeData"))) {
            walk.filter(Files::isRegularFile).forEach(path -> {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
                    String content;
                    while ((content = reader.readLine()) != null) {
                        List<String> l = new ArrayList<>(Arrays.asList(content.split(",")));
                        int nodeID = Integer.parseInt(l.get(0));
                        int neighborID = 0;
                        HashMap<Integer, Integer> linkCost = new HashMap<>();
                        HashMap<Integer, Integer> linkBandwidth = new HashMap<>();
                        l.remove(0);
                        for (String s : l) {
                            if (s.contains("(")) {
                                neighborID = Integer.parseInt(s.substring(s.indexOf("(") + 1));
                            } else if (s.contains(")")) {
                                linkBandwidth.put(neighborID, Integer.parseInt(s.substring(0, s.indexOf(")"))));
                            } else {
                                linkCost.put(neighborID, Integer.parseInt(s));
                            }
                        }
                        nodes.put(nodeID, new Node(nodeID, linkCost, linkBandwidth));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Node node : nodes.values()) {
            System.out.println(node.getNodeId());
            node.start();
        }

        while (true) {
            try {
                Message message = concurrentMessageQueue.take();
                nodes.get(message.getReceiverId()).receiveUpdate(message);
                System.out.println(message.getSenderId() + ": " + nodes.get(message.getReceiverId()).getForwardingTable().toString());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
