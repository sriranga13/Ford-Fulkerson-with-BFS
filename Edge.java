package networkflow.core;

/**
 * Edge class represents the edge in the flow-graph!
 *
 */
public class Edge {
    public int fromNode;
    public int toNode;
    public int capacity;
    
    public Edge(int source, int destination, int capacity) {
        this.fromNode = source;
        this.toNode = destination;
        this.capacity = capacity;        
    }
}
