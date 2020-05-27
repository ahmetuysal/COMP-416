package main;

import domain.FlowRouting;
import domain.Link;
import domain.Message;
import domain.Node;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class ModivSim extends JFrame {

    public static final LinkedBlockingQueue<Message> concurrentMessageQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {

        HashMap<Integer, Node> nodes = new HashMap<>();
        HashMap<Integer, JFrame> nodeDisplays = new HashMap<>();
        HashMap<Integer, JLabel> nodeContents = new HashMap<>();

        JFrame appWindow = new JFrame("ModivSim");
        appWindow.setSize(300, 300);
        JTextArea appOutput = new JTextArea("Running simulation...");
        appWindow.add(appOutput);
        appWindow.setVisible(true);

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
                            nodeDisplays.put(nodeID, new JFrame("Node " + nodeID));
                            nodeDisplays.get(nodeID).setSize(250, 200);
                            nodeDisplays.get(nodeID).setVisible(true);
                            nodeContents.put(nodeID, new JLabel("<html></html>"));
                            nodeDisplays.get(nodeID).add(nodeContents.get(nodeID));
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create scheduler one for round sendUpdate and counter, and one for dynamic link update if there are any dynamic links
        final ScheduledExecutorService service = Executors.newScheduledThreadPool(dynamicLinks.isEmpty() ? 1 : 2);
        AtomicInteger roundCount = new AtomicInteger(0);
        long roundPeriodMilliseconds = 1000;

        service.scheduleAtFixedRate(() -> {
            boolean anyUpdates = false;
            for (Node node : nodes.values()) {
                anyUpdates |= node.sendUpdate();
            }
            if (anyUpdates) {
                roundCount.incrementAndGet();
            }
        }, 0, roundPeriodMilliseconds, TimeUnit.MILLISECONDS);

        if (!dynamicLinks.isEmpty()) {
            service.scheduleAtFixedRate(() -> {
                dynamicLinks.forEach(dynamicLink -> {
                    if (rand.nextBoolean()) {
                        int newCost = rand.nextInt(10) + 1;
                        dynamicLink.setCost(newCost);
                        nodes.get(dynamicLink.getNode1Id()).changeDynamicLinkCost(dynamicLink.getNode2Id(), newCost);
                        nodes.get(dynamicLink.getNode2Id()).changeDynamicLinkCost(dynamicLink.getNode1Id(), newCost);
                        System.out.println("Link between Node " + dynamicLink.getNode1Id() + " and Node " + dynamicLink.getNode2Id() + " changed its cost to " + newCost);
                    }
                });
            }, 0, roundPeriodMilliseconds, TimeUnit.MILLISECONDS);
        }

        long timeoutMilliseconds = 5000;

        while (true) {
            try {
                Message message = concurrentMessageQueue.poll(timeoutMilliseconds, TimeUnit.MILLISECONDS);
                if (message == null) {
                    break;
                }
                int curNode = message.getReceiverId();
                nodes.get(curNode).receiveUpdate(message);
                nodeContents.get(curNode).setText("<html> Distance Vector: " + "<BR>" +
                        nodes.get(curNode).getDistanceVector().toString() + "<BR>" +
                        "Forwarding Table: " + "<BR>" +
                        nodes.get(curNode).getForwardingTable().toString()
                        + "</html>");
                //nodeDisplays.get(curNode).setVisible(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        service.shutdown();

        appOutput.setText("Simulation took " + roundCount.get() + " rounds");

        for (Node node : nodes.values()) {
            System.out.println(node.getNodeId() + ": " + node.getForwardingTable());
        }

        FlowRouting flow = new FlowRouting(nodes, allLinks, appOutput);
        flow.handle();
    }
}
