package domain;

import java.util.List;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class Flow {
    final String name;
    final int sourceId;
    final int destinationId;
    final int totalDataMbits;
    double completionTime;
    double sentDataMbits;
    private int usedBandwidth;
    private List<Link> assignedPath;
    private boolean isDone;

    public Flow(String name, int sourceId, int destinationId, int totalDataMbits) {
        this.name = name;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.totalDataMbits = totalDataMbits;
        this.usedBandwidth = 0;
    }

    public String getName() {
        return name;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public double getRemainingDataMbits() {
        return totalDataMbits - sentDataMbits;
    }

    public double getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(double completionTime) {
        this.completionTime = completionTime;
    }

    public double getSentDataMbits() {
        return sentDataMbits;
    }

    public void increaseSentDataMbits(double sentDataMbits) {
        this.sentDataMbits += sentDataMbits;
    }

    public int getUsedBandwidth() {
        return usedBandwidth;
    }

    public void setUsedBandwidth(int bandwidth) {
        this.usedBandwidth = bandwidth;
    }

    public List<Link> getAssignedPath() {
        return assignedPath;
    }

    public void setAssignedPath(List<Link> assignedPath) {
        this.assignedPath = assignedPath;
    }


    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    @Override
    public String toString() {
        return "Flow{" +
                "name='" + name + '\'' +
                ", sourceId=" + sourceId +
                ", destinationId=" + destinationId +
                ", totalDataMbits=" + totalDataMbits +
                ", completionTime=" + completionTime +
                ", sentDataMbits=" + sentDataMbits +
                ", usedBandwidth=" + usedBandwidth +
                ", assignedPath=" + assignedPath +
                ", isDone=" + isDone +
                '}';
    }
}
