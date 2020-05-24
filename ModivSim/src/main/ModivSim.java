package main;

import domain.Link;
import domain.Message;
import domain.Node;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author Ahmet Uysal @ahmetuysal
 */
public class ModivSim {

    public static final LinkedBlockingQueue<Message> concurrentMessageQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {

        HashMap<Integer, Node> nodes = new HashMap<>();
        Set<Link> dynamicLinks = new HashSet<>();
        Random rand = new Random();

        try (Stream<Path> walk = Files.walk(Paths.get("src/nodeData"))) {
            walk.filter(Files::isRegularFile).forEach(path -> {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
                    String content;
                    List<String> l = null;
                    while ((content = reader.readLine()) != null) {
                        if (path.getFileName().toString().contains("Node")) {
                            l = new ArrayList<>(Arrays.asList(content.split(",")));
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
                                } else if (s.contains("x")) {
                                    Link dynamicLink = new Link(nodeID, neighborID, rand.nextInt(10) + 1);
                                    if (dynamicLinks.contains(dynamicLink)) {
                                        int initialDynamicLinkCost = dynamicLinks.stream()
                                                .filter(link -> link.equals(dynamicLink))
                                                .findFirst()
                                                .get()
                                                .getInitialCost();
                                        linkCost.put(neighborID, initialDynamicLinkCost);
                                    } else {
                                        dynamicLinks.add(dynamicLink);
                                        linkCost.put(neighborID, dynamicLink.getInitialCost());
                                    }
                                } else {
                                    linkCost.put(neighborID, Integer.parseInt(s));
                                }
                            }
                            nodes.put(nodeID, new Node(nodeID, linkCost, linkBandwidth));
                        } else if (path.getFileName().toString().contains("Flow")) {
                            // TODO: Parse flow inputs.
                        }
                    }
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


        // create scheduler for each node and one for dynamic link update if there are any dynamic links
        final ScheduledExecutorService service = Executors.newScheduledThreadPool(nodes.size() + (dynamicLinks.isEmpty() ? 0 : 1));
        for (Node node : nodes.values()) {
            service.scheduleAtFixedRate(node, 0, 200, TimeUnit.MILLISECONDS);
        }

        if (!dynamicLinks.isEmpty()) {
            service.scheduleAtFixedRate(() -> {
                dynamicLinks.forEach(dynamicLink -> {
                    if (rand.nextBoolean()) {
                        int newCost = rand.nextInt(10) + 1;
                        nodes.get(dynamicLink.getNode1Id()).changeDynamicLinkCost(dynamicLink.getNode2Id(), newCost);
                        nodes.get(dynamicLink.getNode2Id()).changeDynamicLinkCost(dynamicLink.getNode1Id(), newCost);
                        System.out.println("Link between Node " + dynamicLink.getNode1Id() + " and Node " + dynamicLink.getNode2Id() + " changed its cost to " + newCost);
                    }
                });
            }, 0, 200, TimeUnit.MILLISECONDS);
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

        service.shutdown();
    }
}
