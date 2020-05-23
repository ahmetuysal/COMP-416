package domain;

import java.util.HashMap;
import java.util.List;

public class Message {

    // TODO: Implement messages.

    private List<Integer> neighbors;
    private HashMap<Integer, Integer> distanceVector;
    private int nodeID;

    public Message(int nodeID, List<Integer> neighbors, HashMap<Integer, Integer> distanceVector)
    {
        this.neighbors = neighbors;
        this.distanceVector = distanceVector;
        this.nodeID = nodeID;
    }

    public List<Integer> getNeighbors() {
        return neighbors;
    }

    public HashMap<Integer, Integer> getDistanceVector() {
        return distanceVector;
    }

    public int getNodeID()
    {
        return nodeID;
    }

    public void setNeighbors(List<Integer> neighbors) {
        this.neighbors = neighbors;
    }

    public void setDistanceVector(HashMap<Integer, Integer> distanceVector) {
        this.distanceVector = distanceVector;
    }

    public void setNodeID(int nodeID)
    {
        this.nodeID = nodeID;
    }

}
