package domain;

import main.ModivSim;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class Node {

    private final int nodeId;
    private final Map<Integer, Integer> linkCost;
    private final Map<Integer, Integer> linkBandwidth;
    private final HashMap<Integer, Integer> distanceVector;
    private final Map<Integer, Map<Integer, Integer>> distanceTable;
    private final List<Integer> neighbors;
    private final Map<Integer, Integer> bottleneckBandwidthTable;
    private boolean isUpdateRequired = true;

    public Node(int nodeId, Map<Integer, Integer> linkCost, Map<Integer, Integer> linkBandwidth) {

        this.nodeId = nodeId;
        this.linkCost = linkCost;
        this.linkBandwidth = linkBandwidth;

        this.neighbors = new ArrayList<>(linkCost.keySet());
        this.distanceVector = new HashMap<>();
        this.distanceVector.put(this.nodeId, 0);
        this.distanceTable = new HashMap<>();
        this.bottleneckBandwidthTable = new HashMap<>();

        linkCost.forEach((neighborId, cost) -> {
            this.distanceVector.put(neighborId, cost);
            Map<Integer, Integer> distanceTableRow = new HashMap<>();
            distanceTableRow.put(neighborId, cost);
            this.distanceTable.put(neighborId, distanceTableRow);
        });
    }

    public void changeDynamicLinkCost(int neighborId, int newCost) {
        this.linkCost.put(neighborId, newCost);
        this.distanceTable.get(neighborId).put(neighborId, newCost);
        int newShortestPathCost = this.distanceTable.get(neighborId).entrySet().stream()
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .get().getValue();
        if (newShortestPathCost < this.distanceVector.get(neighborId)) {
            this.distanceVector.put(neighborId, newShortestPathCost);
            this.isUpdateRequired = true;
        }
        System.out.println("Dynamic change: " + this.nodeId + " changed link to " + neighborId);
    }


    public synchronized void receiveUpdate(Message message) {
        AtomicBoolean isVectorUpdated = new AtomicBoolean(false);

        int neighborID = message.getSenderId();
        int costToNeighbor = this.distanceVector.get(neighborID);
        int pathToNeighbor = this.distanceTable.get(neighborID).entrySet().stream()
                .min(Comparator.comparingInt(Map.Entry::getValue)).get().getKey();

        Map<Integer, Integer> neighborDistanceVector = message.getSenderDistanceVector();

        neighborDistanceVector.forEach((nodeId, cost) -> {
            if (nodeId == this.nodeId)
                return;

            if (!this.distanceTable.containsKey(nodeId)) {
                this.distanceTable.put(nodeId, new HashMap<>());
                this.distanceTable.get(nodeId).put(neighborID, cost + costToNeighbor);
                this.distanceVector.put(nodeId, cost + costToNeighbor);
                isVectorUpdated.set(true);
            } else if (cost + costToNeighbor < this.distanceVector.get(nodeId)) {
                this.distanceVector.put(nodeId, cost + costToNeighbor);
                this.distanceTable.get(nodeId).put(pathToNeighbor, cost + costToNeighbor);
                isVectorUpdated.set(true);
            } else if (!this.distanceTable.get(nodeId).containsKey(pathToNeighbor)) {
                this.distanceTable.get(nodeId).put(pathToNeighbor, cost + costToNeighbor);
            } else if (cost + costToNeighbor < this.distanceTable.get(nodeId).get(pathToNeighbor)) {
                this.distanceTable.get(nodeId).put(pathToNeighbor, cost + costToNeighbor);
            }

        });

        if (isVectorUpdated.get()) {
            this.isUpdateRequired = true;
            System.out.println("Node " + this.nodeId + " updated its table!");
        }
    }

    public boolean sendUpdate() {
        if (!this.isUpdateRequired) {
            return false;
        }

        this.neighbors.forEach(neighborId -> {
            try {
                ModivSim.concurrentMessageQueue.put(new Message(this.nodeId, neighborId, this.linkBandwidth.get(neighborId), new HashMap<>(this.distanceVector)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        this.isUpdateRequired = false;
        return true;
    }

    public Map<Integer, List<Integer>> getForwardingTable() {
        Map<Integer, List<Integer>> forwardingTable = new HashMap<>();
        for (Integer nodeId : this.distanceTable.keySet()) {
            List<Integer> forwardingEntry = this.distanceTable.get(nodeId).entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(this.nodeId))
                    .sorted(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .limit(2)
                    .collect(Collectors.toList());
            forwardingTable.put(nodeId, forwardingEntry);
        }
        return forwardingTable;
    }

    public int getNodeId() {
        return nodeId;
    }

    public Map<Integer, Integer> getLinkBandwidth() {
        return linkBandwidth;
    }

    public HashMap<Integer, Integer> getDistanceVector() {
        return distanceVector;
    }
}
