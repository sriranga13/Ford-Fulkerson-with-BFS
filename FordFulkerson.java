package networkflow.algorithms.maxflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

import networkflow.Constants;
import networkflow.algorithms.BreadthFirstSearch;

/**
 * The class for implementing the ford-fulkerson max-flow algorithm using BFS.
 * Reference:
 * 1. https://www.geeksforgeeks.org/ford-fulkerson-algorithm-for-maximum-flow-problem/
 * 2. https://algorithms.tutorialhorizon.com/max-flow-problem-ford-fulkerson-algorithm/
 */
public class FordFulkerson {
    /**
     * Runs the Ford-Fulkerson algorithm with BFS.
     * returns the maximum flow if the path exists from source to sink, otherwise zero.
     * @param adjacencyList
     * @param source
     * @param sink
     * @param capacityGraph
     * @return
     */
    public static int Run(
        LinkedHashMap<Integer, LinkedHashSet<Integer>> adjacencyList,  
        int source,
        int sink,     
        int[][] capacityGraph
    ) {
        if (adjacencyList.size() <= 0) return 0;        
        
        int maxFlow = 0;

        // cloning the adjacency-list for residual-graph.
        LinkedHashMap<Integer, LinkedHashSet<Integer>> residualGraphAdjacencyList = FordFulkerson.CreateAdjacencyListForResidualGraph(adjacencyList);

        // cloning the capacity graph.
        int[][] rCapacityGraph = Arrays.stream(capacityGraph).map(el -> el.clone()).toArray($ -> capacityGraph.clone());

        Map<Integer, Integer> predecessorTracker = new HashMap<>();
        while (BreadthFirstSearch.Run(residualGraphAdjacencyList, source, sink, rCapacityGraph, predecessorTracker)) {
            
            int pathMaxFlow = Integer.MAX_VALUE;

            ArrayList<Integer> nodesInCurrentPath = new ArrayList<Integer>();
            nodesInCurrentPath.add(sink);

            for (int u = sink; u != source; u = predecessorTracker.get(u)) {
                Integer previous = predecessorTracker.get(u);
                nodesInCurrentPath.add(previous);
                pathMaxFlow = Math.min(pathMaxFlow, rCapacityGraph[previous][u]);
            }
            
            // updating the capacities of edges along the path and reversing the direction.
            for (int u = sink; u != source; u = predecessorTracker.get(u)) {
                Integer v = predecessorTracker.get(u);
                rCapacityGraph[v][u] -= pathMaxFlow;
                rCapacityGraph[u][v] += pathMaxFlow;
            }
            
            FordFulkerson.PrintGraph(nodesInCurrentPath, pathMaxFlow, rCapacityGraph);
            
            // Add path flow to overall flow
            maxFlow += pathMaxFlow;            
        }

        return maxFlow;
    }

    /**
     * Creates an adjacency list for residual graph.
     * @param adjacencyList
     * @return
     */
    private static LinkedHashMap<Integer, LinkedHashSet<Integer>> CreateAdjacencyListForResidualGraph(LinkedHashMap<Integer, LinkedHashSet<Integer>> adjacencyList) {
        LinkedHashMap<Integer, LinkedHashSet<Integer>> residualGraphAdjacencyList = new LinkedHashMap<>();        
        for (Map.Entry<Integer, LinkedHashSet<Integer>> entrySet: adjacencyList.entrySet()) {
            int key = entrySet.getKey();
            LinkedHashSet<Integer> adjacentNodes = adjacencyList.get(entrySet.getKey()).stream().collect(Collectors.toCollection(LinkedHashSet::new));
            residualGraphAdjacencyList.put(key, adjacentNodes);
        }

        return residualGraphAdjacencyList;
    }

    /**
     * Method to print graph details.
     * @param adjacencyList
     * @param rCapacityGraph
     */
    private static void PrintGraph(ArrayList<Integer> nodesInCurrentPath, int bottleneckCapacity, int[][] rCapacityGraph) {
        System.out.println("Residual Graph: ");
        System.out.print("Path (Augmented): ");
        for (int i = nodesInCurrentPath.size()-1; i >= 0; i--) {                         
            System.out.print(nodesInCurrentPath.get(i));
            if (i != 0) {
                System.out.print(" -> ");                
            }
        }
        
        System.out.println("\nBottlneck Capacity along the path: " + bottleneckCapacity);
        System.out.println("Capacity graph: ");
        for (int u = 0; u < rCapacityGraph.length; u++) {
            for (int v = 0; v < rCapacityGraph.length; v++) {
                if (v != u && rCapacityGraph[u][v] != Constants.NO_EDGE) {
                    System.out.printf("%s -> %s: %s \t", u, v, rCapacityGraph[u][v]);                    
                }
            }
            System.out.println();
        }
        
    }
}
