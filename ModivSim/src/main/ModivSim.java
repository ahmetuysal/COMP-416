package main;

import domain.Message;
import domain.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
                    Random rand = new Random();
                    while ((content = reader.readLine()) != null) {
                        List<String> l = new ArrayList<>(Arrays.asList(content.split(",")));
                        int nodeID = Integer.parseInt(l.get(0));
                        int neighborID = 0;
                        HashMap<Integer, List<Integer>> linkCost = new HashMap<>();
                        HashMap<Integer, Integer> linkBandwidth = new HashMap<>();
                        l.remove(0);
                        for (String s : l) {
                            if (s.contains("(")) {
                                neighborID = Integer.parseInt(s.substring(s.indexOf("(") + 1));
                            } else if (s.contains(")")) {
                                linkBandwidth.put(neighborID, Integer.parseInt(s.substring(0, s.indexOf(")"))));
                            } else if(s.contains("x")) {
                                linkCost.put(neighborID, new ArrayList<>(2)); // need to tell make the link dynamic within the node as well.
                                linkCost.get(neighborID).add(1); // dynamic link
                                linkCost.get(neighborID).add(rand.nextInt(10) + 1);
                            } else {
                                linkCost.put(neighborID, new ArrayList<>(2));
                                linkCost.get(neighborID).add(0); // static link
                                linkCost.get(neighborID).add(Integer.parseInt(s));
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
            node.start();
        }


        while (true) {
            try {
                Message message = concurrentMessageQueue.poll(5, TimeUnit.SECONDS);
                if (message == null)
                    break;
                nodes.get(message.getReceiverId()).receiveUpdate(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (Node node : nodes.values()) {
            System.out.println(node.getNodeId() + ": " + node.getForwardingTable());
        }
    }
}
