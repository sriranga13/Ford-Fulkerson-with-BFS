package networkflow.core;

import java.lang.Exception;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import networkflow.Constants;

/**
 * Flow Graph class!
 * Reference: 
 * 1. https://www.javatpoint.com/collections-in-java
 * 2. https://medium.com/swlh/real-world-network-flow-cricket-elimination-problem-55a3036a5d60
 * 3. https://www.geeksforgeeks.org/ford-fulkerson-algorithm-for-maximum-flow-problem/
 * 
 */
public class FlowGraph {    
    public List<Node> vertices;
    public List<Edge> edges;

    public LinkedHashMap<Integer, LinkedHashSet<Integer>> adjacencyList;
    public int[][] capacityGraph;
    
    private int source;
    private int sink;
    
    public FlowGraph() {
        this.vertices = new LinkedList<Node>();
        this.edges = new LinkedList<Edge>();
        this.adjacencyList = new LinkedHashMap<Integer, LinkedHashSet<Integer>>();
        
        // setting default source and sink.
        this.setSink(Constants.DEFAULT_SINK);
        this.setSource(Constants.DEFAULT_SOURCE);
    }

    public void buildGraph(List<String[]> lineWiseFileInput) throws Exception {
        try {
            int currentNode = 0;
            for (String[] inputTokens: lineWiseFileInput) {
                this.createNode(currentNode); // required?
                if (inputTokens.length > 0) {
                    for (int index = 0; index < inputTokens.length;) {
                        int adjacentNode = Integer.parseInt(inputTokens[index++]);
                        int capacity = Integer.parseInt(inputTokens[index++]);
                        this.addEdge(currentNode, adjacentNode, capacity); 
                        this.updateAdjacencyList(currentNode, adjacentNode);
                    }
                } else { // we will get here when the input file has no adjacent node data in the line, or simply blank line.
                    LinkedHashSet<Integer> adjacentNodesList = new LinkedHashSet<>();
                    this.adjacencyList.put(currentNode, adjacentNodesList);                                
                    if (this.sink < 0) { // since, no outward edges, identifying this node as sink.
                        this.setSink(currentNode);
                    }
                }
                currentNode++;
            }
            this.checkAndSaveSinkNode(currentNode);
            this.identifySourceNode();
            this.createCapacityGraph();

            System.out.println("Builded graph with input file data: ");
            this.printGraph();
        } catch (Exception e) {
            throw new Exception("Failed to build the graph, stack trace: " + e.toString());
        }
    }

    public LinkedHashMap<Integer, LinkedHashSet<Integer>> getAdjacentList() {
        return this.adjacencyList;
    }

    public int[][] getCapacityGraph() {
        return this.capacityGraph;
    }

    public int getSource() {
        return this.source;
    }

    public int getSink() {
        return this.sink;
    }
    
    private void setSource(int source) {
        this.source = source;
    }
    
    private void setSink(int sink) {
        this.sink = sink;
    }

    private Node getNode(int nodeId) {
        for (Node node: this.vertices) {
            if (node.id == nodeId) {
                return node;
            }
        }
        return null;
    }

    private void createNode(int nodeId) {        
        Node node = this.getNode(nodeId);
        if (node == null) {
            Node newNode = new Node(nodeId);
            this.vertices.add(newNode);
        }
    }

    private Optional<Edge> getEdge(int fromNode, int toNode) {
        for (Edge edge: this.edges) {
            if (edge.fromNode == fromNode && edge.toNode == toNode) {
                return Optional.of(edge);
            }
        }
        return null;
    }

    private void addEdge(int fromNode, int toNode, int capacity) throws Exception {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity should be a non-negative number.");
        } else if (fromNode == toNode) { 
            throw new IllegalArgumentException("Invalid operation, from and to nodes should be different for adding an edge.");
        }

        Edge newEdge = new Edge(fromNode, toNode, capacity);
        this.edges.add(newEdge); // store backward edge too?
    }

    private void createCapacityGraph() {
        int verticesCount = this.vertices.size();
        this.capacityGraph = new int[verticesCount][verticesCount];

        for (int[] capacityArrayListItem: this.capacityGraph) {
            Arrays.fill(capacityArrayListItem, Constants.NO_EDGE);
        }

        for (int u = 0; u < verticesCount; u++) {
            for (int v = 0; v < verticesCount; v++) {
                if (v != u) {
                    Optional<Edge> edge = this.getEdge(u, v);
                    this.capacityGraph[u][v] = edge != null ? edge.get().capacity : Constants.NO_EDGE;                    
                }
            }
        }
    }

    private void updateAdjacencyList(int currentNode, int adjacentNode) {
        if (this.adjacencyList.containsKey(currentNode)) {
            LinkedHashSet<Integer> existingAdjacentNodes = this.adjacencyList.get(currentNode);
            existingAdjacentNodes.add(adjacentNode);
            this.adjacencyList.put(currentNode, existingAdjacentNodes);
        } else {
            LinkedHashSet<Integer> adjacentNodesList = new LinkedHashSet<>();
            adjacentNodesList.add(adjacentNode);
            this.adjacencyList.put(currentNode, adjacentNodesList);            
        }
    }

    private void checkAndSaveSinkNode(Integer currentNode) {
        if (this.sink < 0) { // since, no outward edges, identifying this node as sink.
            this.createNode(currentNode);         
            this.setSink(currentNode);
        }
    }

    private void identifySourceNode() {
        if (this.adjacencyList.size() > 0 && this.source < 0) {
            // tracking node wise inward edges frequency count.
            Map<Integer, Integer> nodeWiseInwardEdgesCounter = new LinkedHashMap<>(this.adjacencyList.size());
            List<Integer> adjacentNodeList = this.adjacencyList.values().stream()
                                                    .flatMap(lists -> lists.stream())
                                                    .collect(Collectors.toList());
            for (Node node: this.vertices) {
                Integer vertex = node.id;
                Integer inwardEdgesCount = Collections.frequency(adjacentNodeList, vertex);
                if (!nodeWiseInwardEdgesCounter.containsKey(vertex)) {
                    nodeWiseInwardEdgesCounter.put(vertex, inwardEdgesCount);
                }
            }

            // finding the (first, if many) edge with no inward edges and identifying it as source node.
            Set<Entry<Integer, Integer>> entries = nodeWiseInwardEdgesCounter.entrySet();
            List<Entry<Integer, Integer>> sortedEntries = new ArrayList<>(entries);
            Collections.sort(
                sortedEntries, 
                (entry1, entry2) -> {
                    int count1 = entry1.getValue(), count2 = entry2.getValue(); 
                    return count1 < count2 ? -1 : (count1 > count2 ? 1 : 0);
                }
            );

            /** 
             * since we are labeling and inserting nodes in asc order, 
            * and linkedhashmap preserves order, taking first element directly. 
            */
            this.setSource(sortedEntries.get(0).getKey()); 
        }
    }

    private void printGraph() {
        System.out.println("Graph (V, E): (" + this.vertices.size() + ", " + this.edges.size() + ")");
        System.out.println("Adjacency List:");
        for (Map.Entry<Integer, LinkedHashSet<Integer>> map: this.adjacencyList.entrySet()) {                     
            System.out.println(map.getKey() + " -> " + map.getValue());
        }
        System.out.println();
    }
}
