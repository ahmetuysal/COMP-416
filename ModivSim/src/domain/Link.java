package domain;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ahmet Uysal @ahmetuysal
 */
public class Link {
    private final int node1Id;
    private final int node2Id;
    private final int initialCost;

    public Link(int node1Id, int node2Id, int initialCost) {
        this.node1Id = node1Id;
        this.node2Id = node2Id;
        this.initialCost = initialCost;
    }

    public int getNode1Id() {
        return node1Id;
    }

    public int getNode2Id() {
        return node2Id;
    }

    public int getInitialCost() {
        return initialCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return (node1Id == link.node1Id &&
                node2Id == link.node2Id) || (node1Id == link.node2Id && node2Id == link.node1Id);
    }

    @Override
    public int hashCode() {
        // TODO: find a better logic
        Set<Integer> nodeIdSet = new HashSet<>();
        nodeIdSet.add(node1Id);
        nodeIdSet.add(node2Id);
        return nodeIdSet.hashCode();
    }
}
