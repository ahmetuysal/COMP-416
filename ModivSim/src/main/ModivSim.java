package main;

import domain.FlowRouting;
import domain.Link;
import domain.Message;
import domain.Node;

import java.io.BufferedReader;
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
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class ModivSim {

    public static final LinkedBlockingQueue<Message> concurrentMessageQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {

        HashMap<Integer, Node> nodes = new HashMap<>();
        Set<Link> dynamicLinks = new HashSet<>();
        Set<Link> allLinks = new HashSet<>();
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
                            boolean isDynamic = false;
                            int assignedCost = -1;
                            l.remove(0);
                            for (String s : l) {
                                if (s.contains("(")) {
                                    neighborID = Integer.parseInt(s.substring(s.indexOf("(") + 1));
                                } else {
                                    if (s.contains(")")) {
                                        int bandwidth = Integer.parseInt(s.substring(0, s.indexOf(")")));
                                        linkBandwidth.put(neighborID, bandwidth);
                                        Link link = new Link(nodeID, neighborID, assignedCost, bandwidth);
                                        // We don't need to check for duplicate links since they are added to sets and
                                        // Link.equals method is symmetric for nodeId1 and nodeId2
                                        if (isDynamic) {
                                            dynamicLinks.add(link);
                                        }
                                        allLinks.add(link);
                                    } else if (s.contains("x")) {
                                        isDynamic = true;
                                        int finalNeighborID = neighborID;
                                        Optional<Link> alreadyAddedDynamicLink = dynamicLinks.stream()
                                                .filter(link -> ((link.getNode1Id() == nodeID && link.getNode2Id() == finalNeighborID) ||
                                                        (link.getNode2Id() == nodeID && link.getNode1Id() == finalNeighborID)))
                                                .findFirst();

                                        assignedCost = alreadyAddedDynamicLink.map(Link::getCost).orElseGet(() -> rand.nextInt(10) + 1);
                                        linkCost.put(neighborID, assignedCost);
                                    } else {
                                        isDynamic = false;
                                        assignedCost = Integer.parseInt(s);
                                        linkCost.put(neighborID, assignedCost);
                                    }
                                }
                            }
                            nodes.put(nodeID, new Node(nodeID, linkCost, linkBandwidth));
                        } else if (path.getFileName().toString().contains("Flow")) {
                            // TODO: Parse flow inputs.
                        }
                    }
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

        service.shutdown();

        for (Node node : nodes.values()) {
            System.out.println(node.getNodeId() + ": " + node.getForwardingTable());
        }

        FlowRouting flow = new FlowRouting(nodes, allLinks);
        flow.handle();
    }
}
