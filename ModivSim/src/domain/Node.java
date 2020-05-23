package domain;

import main.ModivSim;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Node extends Thread {

    private final int nodeId;
    private final Map<Integer, List<Integer>> linkCost;
    private final Map<Integer, Integer> linkBandwidth;
    private final HashMap<Integer, Integer> distanceVector;
    private final Map<Integer, Map<Integer, Integer>> distanceTable;
    private final List<Integer> neighbors;
    private final Map<Integer, Integer> bottleneckBandwidthTable;
    private boolean isUpdateRequired = true;
    private boolean dynamicLinks = false;

    public Node(int nodeId, Map<Integer, List<Integer>> linkCost, Map<Integer, Integer> linkBandwidth) {

        this.nodeId = nodeId;
        this.linkCost = linkCost;
        this.linkBandwidth = linkBandwidth;

        this.neighbors = new ArrayList<>(linkCost.keySet());
        this.distanceVector = new HashMap<>();
        this.distanceVector.put(this.nodeId, 0);
        this.distanceTable = new HashMap<>();
        this.bottleneckBandwidthTable = new HashMap<>();

        linkCost.forEach((neighborId, cost) -> {
            this.distanceVector.put(neighborId, cost.get(1));
            Map<Integer, Integer> distanceTableRow = new HashMap<>();
            distanceTableRow.put(neighborId, cost.get(1));
            this.distanceTable.put(neighborId, distanceTableRow);
        });
    }

    public void run() {
        final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(this::sendUpdate, 0, 200, TimeUnit.MILLISECONDS);
    }


    public synchronized void receiveUpdate(Message message) {

        AtomicBoolean isTableUpdated = new AtomicBoolean(false);
        Random rand = new Random();
        
        // assuming the round decision is made here.
        linkCost.forEach((neighborId, cost) -> {
            if(cost.get(0) == 1)
            {
                if(rand.nextBoolean()) {
                    // changing dynamic cost.
                    cost.set(1, rand.nextInt(10) + 1);
                    this.distanceVector.put(neighborId, cost.get(1));
                    this.distanceTable.get(neighborId).put(neighborId, cost.get(1));
                    isTableUpdated.set(true);
                }
            }
        });

        int neighborID = message.getSenderId();
        int costToNeighbor = this.distanceVector.get(neighborID);
        int pathToNeighbor = this.distanceTable.get(neighborID).entrySet().stream()
                .min(Comparator.comparingInt(Map.Entry::getValue)).get().getKey();

        Map<Integer, Integer> neighborDistanceVector = message.getSenderDistanceVector();

        // need to update the dynamic cost among neighbors as well (if the neighbor has dynamically changed it).

        if(this.linkCost.get(neighborID).get(0) == 1 && this.distanceVector.get(neighborID) != neighborDistanceVector.get(this.nodeId)) {
            this.distanceVector.put(neighborID, neighborDistanceVector.get(this.nodeId));
            this.distanceTable.get(neighborID).put(neighborID, neighborDistanceVector.get(this.nodeId));
            isTableUpdated.set(true);
        }

        neighborDistanceVector.forEach((nodeId, cost) -> {

            if (nodeId == this.nodeId)
                return;

            if (!this.distanceTable.containsKey(nodeId)) {
                this.distanceTable.put(nodeId, new HashMap<>());
                this.distanceTable.get(nodeId).put(neighborID, cost + costToNeighbor);
                isTableUpdated.set(true);
            } else if (!this.distanceVector.containsKey(nodeId)) {
                this.distanceVector.put(nodeId, cost + costToNeighbor);
                isTableUpdated.set(true);
            } else if (cost + costToNeighbor < this.distanceVector.get(nodeId)) {
                this.distanceVector.put(nodeId, cost + costToNeighbor);
                this.distanceTable.get(nodeId).put(pathToNeighbor, cost + costToNeighbor);
                isTableUpdated.set(true);
            } else if (!this.distanceTable.get(nodeId).containsKey(pathToNeighbor)) {
                this.distanceTable.get(nodeId).put(pathToNeighbor, cost + costToNeighbor);
                this.distanceVector.put(pathToNeighbor, cost + costToNeighbor);
                // this.distanceVector.put()
                isTableUpdated.set(true);
            } else if(cost + costToNeighbor < this.distanceTable.get(nodeId).get(pathToNeighbor) ) {
                this.distanceTable.get(nodeId).put(pathToNeighbor, cost + costToNeighbor);
                this.distanceVector.put(pathToNeighbor, cost + costToNeighbor);
                isTableUpdated.set(true);
            }

        });

        if (isTableUpdated.get()) {
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
                    .collect(Collectors.toList());
            forwardingTable.put(nodeId, forwardingEntry.subList(0,2));
        }
        return forwardingTable;
    }

    public int getNodeId() {
        return nodeId;
    }

}
