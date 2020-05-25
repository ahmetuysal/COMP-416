package domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FlowRouting {

    private final HashMap<Integer, Node> nodes;
    private final Set<Link> links;
    private final List<Flow> flows;
    private final List<Flow> activeFlows;
    private final List<Flow> queuedFlows;
    private HashMap<Integer, Map<Integer, List<Integer>>> forwardingTables;

    public FlowRouting(HashMap<Integer, Node> nodes, Set<Link> links) {
        this.nodes = nodes;
        this.links = links;
        this.flows = new ArrayList<>();
        this.activeFlows = new ArrayList<>();
        this.queuedFlows = new ArrayList<>();
        this.forwardingTables = new HashMap<>();
        for (Node node : this.nodes.values()) {
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
            System.out.println(flows);
            for (Flow flow : flows) {
                if (flow.isDone()) {
                    continue;
                }
                List<Link> path = findShortestAvailablePath(flow.getSourceId(), flow.getDestinationId());
                if (path == null || path.isEmpty()) {
                    System.out.println("Flow " + flow.getName() + " is queued");
                    queuedFlows.add(flow);
                } else {
                    // remove flow it it's in queue
                    queuedFlows.remove(flow);

                    int pathBottleneck = path.stream().map(Link::getAvailableBandwidth).min(Integer::compareTo).get();
                    path.forEach(link -> link.reserveBandwidth(pathBottleneck));
                    flow.setAssignedPath(path);
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
            activeFlows.forEach(fl -> {
                fl.increaseSentDataMbits(fl.getUsedBandwidth() * timeToNextStep);
                fl.getAssignedPath().forEach(link -> link.releaseBandwidth(fl.getUsedBandwidth()));
            });

            activeFlows.clear();
        }
        System.out.println("Flow simulation ended in " + totalTimeSpent);
    }

    private List<Link> findShortestAvailablePath(int sourceId, int destinationId) {
        List<List<Link>> allAvailablePaths = findAllAvailablePaths(sourceId, destinationId, new ArrayList<>());
        if (allAvailablePaths.isEmpty())
            return null;

        return allAvailablePaths.stream()
                .min(Comparator.comparingInt(path -> path.stream().map(Link::getCost).reduce(0, Integer::sum))).get();
    }

    private List<List<Link>> findAllAvailablePaths(int sourceId, int destinationId, List<Integer> previousNodes) {
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