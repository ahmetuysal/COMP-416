package domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class Node {

    private int nodeID;
    private Hashtable<Integer, Integer> linkCost;
    private Hashtable<Integer, Integer> linkBandwidth;
    private HashMap<Integer, Integer> distanceVector;
    private HashMap<Integer,List<Integer>> distanceTable;
    private List<Integer> neighbors;
    private int[] bottleneckBandwidthTable;
    private int numNeighbors;
    protected ObjectInputStream objectInputStream;
    protected ObjectOutputStream objectOutputStream;
    private Socket socket;
    private boolean socketClosedByServer;
    private boolean update = false;

    public Node(int nodeID, Hashtable<Integer, Integer> linkCost, Hashtable<Integer, Integer> linkBandwidth)
    {
        this.nodeID = nodeID;
        this.linkCost = linkCost;
        this.linkBandwidth = linkBandwidth;

        // TODO: init distance table as well.

        Set<Integer> keySet = linkCost.keySet();
        neighbors = new ArrayList<>(keySet);
        this.distanceVector = new HashMap<>();
        distanceTable = new HashMap<>(); // would require instantiating each arraylist element.
        //neighbors.add(nodeID);
        //ArrayList<Integer> insList = (ArrayList<Integer>) Collections.nCopies(Collections.max(neighbors) + 1, 999);
        //neighbors.remove(neighbors.indexOf(nodeID));
        distanceTable.put(nodeID, new ArrayList<>(2));
        // Collections.fill(distanceTable.get(nodeID), 999);
        distanceTable.get(nodeID).add(nodeID);
        distanceTable.get(nodeID).add(0);
        for (int neighborID : neighbors) {
            if(neighborID != nodeID) {
                distanceTable.put(neighborID, new ArrayList<>(2));
                distanceTable.get(neighborID).add(nodeID);
                distanceTable.get(neighborID).add(linkCost.get(neighborID));
            }
        }

        connect("localhost", 4444);

    }

    public void receiveUpdate(Message receivedMessage)
    {
        if(!neighbors.contains(receivedMessage.getNodeID())) return;

        int neighborID = receivedMessage.getNodeID();
        HashMap<Integer, Integer> neighborDV = receivedMessage.getDistanceVector();
        Set<Integer> keySet = neighborDV.keySet();
        ArrayList<Integer> neighborNs = new ArrayList<>(keySet);
        for(int nnID : neighborNs) {
            int distanceToNode = distanceTable.get(neighborID).get(1) + neighborDV.get(nnID);
            if (!distanceTable.containsKey(nnID))
            {
                distanceTable.put(nnID, new ArrayList<>(2));
                distanceTable.get(nnID).add(neighborID);
                distanceTable.get(nnID).add(distanceToNode);
                update = true;
            }
            else
            {
                if(distanceTable.get(nnID).get(1) != distanceToNode)
                {
                    distanceTable.get(nnID).add(0, neighborID);
                    distanceTable.get(nnID).add(1,distanceToNode);
                    update = true;
                }
            }
        }

        // inform neighbors
        sendUpdate();
        return;

    }

    public boolean sendUpdate()
    {
        if(update){
            for(int i : distanceTable.keySet())
            {
                distanceVector.put(i, distanceTable.get(i).get(1));
            }
            Message information = new Message(nodeID, neighbors, distanceVector);
            try {
                objectOutputStream.writeObject(information);
                objectOutputStream.flush();
            } catch (SocketException e) {
                this.socketClosedByServer = true;
                disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        } else {
            return false;
        }

        // how to directly call neighboring nodes' receive message function?

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

    public Hashtable<Integer, Integer> getForwardingTable()
    {

        return new Hashtable<Integer, Integer>();

    }

    public int getNodeID()
    {
        return nodeID;
    }

    public List<Integer> getNeighbors()
    {
        return neighbors;
    }

}
