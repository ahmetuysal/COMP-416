package domain;

import main.ModivSim;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Node extends Thread {

    private final int nodeId;
    private final Map<Integer, Integer> linkCost;
    private final Map<Integer, Integer> linkBandwidth;
    private final HashMap<Integer, Integer> distanceVector;
    private final Map<Integer, KeyValuePair<Integer, Integer>> distanceTable;
    private final List<Integer> neighbors;
    private final Map<Integer, Integer> bottleneckBandwidthTable;
    protected ObjectInputStream objectInputStream;
    protected ObjectOutputStream objectOutputStream;
    private boolean isUpdateRequired = true;

    public Node(int nodeId, Map<Integer, Integer> linkCost, Map<Integer, Integer> linkBandwidth) {
        this.nodeId = nodeId;
        this.linkCost = linkCost;
        this.linkBandwidth = linkBandwidth;

        this.neighbors = new ArrayList<>(linkCost.keySet());
        this.distanceVector = new HashMap<>();
        this.distanceTable = new HashMap<>();
        this.bottleneckBandwidthTable = new HashMap<>();

        linkCost.forEach((neighborId, cost) -> {
            this.distanceVector.put(neighborId, cost);
            this.distanceTable.put(neighborId, new KeyValuePair<>(neighborId, cost));
        });
    }

    public void run() {
        final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(this::sendUpdate, 0, 200, TimeUnit.MILLISECONDS);
    }


    public synchronized void receiveUpdate(Message message) {
        if (!neighbors.contains(message.getSenderId())) return;

        int neighborID = message.getSenderId();
        int costToNeighbor = this.distanceVector.get(neighborID);
        int pathToNeighbor = this.distanceTable.get(neighborID).getKey();

        AtomicBoolean isTableUpdated = new AtomicBoolean(false);

        Map<Integer, Integer> neighborDistanceVector = message.getSenderDistanceVector();
        neighborDistanceVector.forEach((nodeId, cost) -> {
            if (!this.distanceVector.containsKey(nodeId) || cost + costToNeighbor < this.distanceVector.get(nodeId)) {
                this.distanceVector.put(nodeId, cost + costToNeighbor);
                this.distanceTable.put(nodeId, new KeyValuePair<>(pathToNeighbor, cost + costToNeighbor));
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

        return true;
    }
}
