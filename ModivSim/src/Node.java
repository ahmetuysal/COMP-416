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

    }

    public void receiveUpdate(Message receivedMessage)
    {
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
            }
            else
            {
                if(distanceTable.get(nnID).get(1) != distanceToNode)
                {
                    distanceTable.get(nnID).add(0, neighborID);
                    distanceTable.get(nnID).add(1,distanceToNode);
                }
            }
        }

        for(int i : distanceTable.keySet())
        {
            distanceVector.put(i, distanceTable.get(i).get(1));
        }

        sendUpdate();
        // inform neighbors

    }

    public boolean sendUpdate()
    {
        Message information = new Message(nodeID, neighbors, distanceVector);
        // how to directly call neighboring nodes?
        return false;
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
