package domain;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class FlowRouting {

    private final Set<Link> links;
    private final List<Flow> flows;
    private final List<Flow> activeFlows;
    private final List<Flow> queuedFlows;
    private final JTextArea appOutput;
    private final HashMap<Integer, Map<Integer, List<Integer>>> forwardingTables;

    public FlowRouting(HashMap<Integer, Node> nodes, Set<Link> links, JTextArea appOutput) {
        this.links = links;
        this.appOutput = appOutput;
        this.flows = new ArrayList<>();
        this.activeFlows = new ArrayList<>();
        this.queuedFlows = new ArrayList<>();
        this.forwardingTables = new HashMap<>();
        for (Node node : nodes.values()) {
            forwardingTables.put(node.getNodeId(), node.getForwardingTable());
        }
        registerFlows();
    }

    private void registerFlows() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/flowData/flows.txt"));
            String content;
            List<String> l = null;
            while ((content = reader.readLine()) != null) {
                l = new ArrayList<>(Arrays.asList(content.split(",")));
                flows.add(new Flow(l.get(0), Integer.parseInt(l.get(1)), Integer.parseInt(l.get(2)), Integer.parseInt(l.get(3))));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle() {
        double totalTimeSpent = 0;

        while (true) {
            //appOutput.append("\n" + flows);
            for (Flow flow : flows) {
                if (flow.isDone()) {
                    continue;
                }
                List<Link> path = findShortestAvailablePath(flow.getSourceId(), flow.getDestinationId());
                if (path == null || path.isEmpty()) {

                    appOutput.append("\nFlow " + flow.getName() + " is queued");
                    queuedFlows.add(flow);
                } else {
                    // remove flow if it's in queue
                    queuedFlows.remove(flow);

                    int pathBottleneck = path.stream().map(Link::getAvailableBandwidth).min(Integer::compareTo).get();
                    path.forEach(link -> link.reserveBandwidth(pathBottleneck));
                    flow.setAssignedPath(path);
                    ArrayList<Integer> pathNodes = new ArrayList<Integer>();
                    int prev = flow.getSourceId();
                    int current = flow.getSourceId();
                    pathNodes.add(current);
                    for (int i = 0; i < path.size(); i++) {
                        current = path.get(i).getNode1Id();
                        if (current == prev)
                            current = path.get(i).getNode2Id();
                        pathNodes.add(current);
                        prev = current;
                    }
                    appOutput.append("\nA new path assigned to flow " + flow.getName() + ": " + pathNodes);
                    flow.setCompletionTime(flow.getRemainingDataMbits() / pathBottleneck);
                    flow.setUsedBandwidth(pathBottleneck);
                    activeFlows.add(flow);
                }
            }
            if (activeFlows.isEmpty())
                break;
            Flow flowToFinish = activeFlows.stream().min(Comparator.comparingDouble(Flow::getCompletionTime)).get();
            flowToFinish.setDone(true);
            double timeToNextStep = flowToFinish.getCompletionTime();
            totalTimeSpent += timeToNextStep;
            appOutput.append("\nFlow " + flowToFinish.getName() + " is finished after " + totalTimeSpent);
            activeFlows.forEach(fl -> {
                fl.increaseSentDataMbits(fl.getUsedBandwidth() * timeToNextStep);
                fl.getAssignedPath().forEach(link -> link.releaseBandwidth(fl.getUsedBandwidth()));
            });

            activeFlows.clear();
        }

        appOutput.append("\nFlow simulation ended in " + totalTimeSpent);
    }

    private List<Link> findShortestAvailablePath(int sourceId, int destinationId) {
        List<List<Link>> allAvailablePaths = findAllAvailablePaths(sourceId, destinationId, new ArrayList<>());
        if (allAvailablePaths.isEmpty())
            return null;

        return allAvailablePaths.stream()
                .min(Comparator.comparingInt(path -> path.stream().map(Link::getCost).reduce(0, Integer::sum))).get();
    }

    private List<List<Link>> findAllAvailablePaths(int sourceId, int destinationId, List<
            Integer> previousNodes) {
        ArrayList<Integer> previousNodesClone = new ArrayList<>(previousNodes);
        previousNodesClone.add(sourceId);
        List<List<Link>> allPaths = new ArrayList<>();
        this.forwardingTables.get(sourceId).get(destinationId).stream().filter(next -> !previousNodes.contains(next))
                .forEach(next -> {
                    Link linkToNext = getLink(sourceId, next);
                    if (linkToNext.getAvailableBandwidth() == 0)
                        return;
                    if (next == destinationId) {
                        List<Link> path = new ArrayList<>();
                        path.add(linkToNext);
                        allPaths.add(path);
                    } else {
                        List<List<Link>> pathsFromNextNode = findAllAvailablePaths(next, destinationId, previousNodesClone);
                        for (List<Link> pathFromNextNode : pathsFromNextNode) {
                            pathFromNextNode.add(0, linkToNext);
                        }
                        allPaths.addAll(pathsFromNextNode);
                    }
                });

        return allPaths;
    }

    private Link getLink(int node1Id, int node2Id) {
        Optional<Link> link = this.links.stream()
                .filter(l -> ((l.getNode1Id() == node1Id && l.getNode2Id() == node2Id) ||
                        (l.getNode2Id() == node1Id && l.getNode1Id() == node2Id))).findFirst();

        return link.orElse(null);
    }

}