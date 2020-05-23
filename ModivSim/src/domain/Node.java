package domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Node extends Thread {

    private final int nodeId;
    private final Map<Integer, Integer> linkCost;
    private final Map<Integer, Integer> linkBandwidth;
    private final Map<Integer, Integer> distanceVector;
    private final Map<Integer, KeyValuePair<Integer, Integer>> distanceTable;
    private final List<Integer> neighbors;
    private final Map<Integer, Integer> bottleneckBandwidthTable;
    protected ObjectInputStream objectInputStream;
    protected ObjectOutputStream objectOutputStream;
    private boolean isUpdateRequired = false;
    private Socket socket;
    private boolean socketClosedByServer;
    private boolean update = false;

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

    public void receiveUpdate(Message message) {
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
        }
    }

    public boolean sendUpdate() {
        if (!this.isUpdateRequired) {
            return false;
        }

        // TODO: send update
        Message message = new Message(this.nodeId, 0, 0, this.distanceVector);
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (SocketException e) {
            this.socketClosedByServer = true;
            disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;


    }

    public Map<Integer, List<Integer>> getForwardingTable()
    {
        return new HashMap<Integer, List<Integer>>();
    }

    private void connect(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
        } catch (IOException e) {
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }

    public void disconnect() {
        try {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
            socketClosedByServer = true;
            System.out.println("Connection Closed");
        } catch (IOException e) {
            System.out.println("Connection Already Closed by server");
        } finally {
            objectInputStream = null;
            objectOutputStream = null;
            socket = null;
        }
    }


}
