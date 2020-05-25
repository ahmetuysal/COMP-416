package domain;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class Link {
    private final int node1Id;
    private final int node2Id;
    private final int bandwidth;
    private int bandwidthInUse;
    private int cost;

    public Link(int node1Id, int node2Id, int cost, int bandwidth) {
        this.node1Id = node1Id;
        this.node2Id = node2Id;
        this.bandwidth = bandwidth;
        this.cost = cost;
        this.bandwidthInUse = 0;
    }

    public int getNode1Id() {
        return node1Id;
    }

    public int getNode2Id() {
        return node2Id;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public boolean reserveBandwidth(int requestedBandwidth) {
        if (this.bandwidth - this.bandwidthInUse < requestedBandwidth)
            return false;

        this.bandwidthInUse += requestedBandwidth;
        return true;
    }

    public void releaseBandwidth(int releasedBandwidth) {
        this.bandwidthInUse -= releasedBandwidth;
    }

    public int getAvailableBandwidth() {
        return this.bandwidth - this.bandwidthInUse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return (this.node1Id == link.node1Id &&
                this.node2Id == link.node2Id) || (this.node1Id == link.node2Id && this.node2Id == link.node1Id)
                && this.cost == link.cost && this.bandwidth == link.bandwidth && this.bandwidthInUse == link.bandwidthInUse;
    }

    @Override
    public int hashCode() {
        // TODO: find a better logic
        Set<Integer> nodeIdSet = new HashSet<>();
        nodeIdSet.add(node1Id);
        nodeIdSet.add(node2Id);
        return 31 * nodeIdSet.hashCode() + this.bandwidth;
    }

    @Override
    public String toString() {
        return "Link{" +
                "node1Id=" + node1Id +
                ", node2Id=" + node2Id +
                ", bandwidth=" + bandwidth +
                ", bandwidthInUse=" + bandwidthInUse +
                ", cost=" + cost +
                '}';
    }
}
