package networkflow.algorithms;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;

/**
 * This class implements the BreadFirstSearch algorithm!
 */
public class BreadthFirstSearch {

    public static boolean Run(
        Map<Integer, LinkedHashSet<Integer>> adjacencyList,
        int source,
        int sink,
        int[][] capacityGraph,
        Map<Integer, Integer> predecessorTracker
    ) {
        int verticesCount = capacityGraph.length;
        Queue<Integer> queue = new ArrayDeque<Integer>(verticesCount);
        boolean[] visitedNodes = new boolean[verticesCount];

        queue.offer(source);
        visitedNodes[source] = true;
        
        while (!queue.isEmpty()) {
            int currentNode = queue.poll();
            LinkedHashSet<Integer> adjacentNodes = new LinkedHashSet<>();
            if (adjacencyList.containsKey(currentNode)) {
                adjacentNodes = adjacencyList.get(currentNode);
            }
            
            for (Integer adjacentNode: adjacentNodes) {
                if (!visitedNodes[adjacentNode] && capacityGraph[currentNode][adjacentNode] > 0) {
                    predecessorTracker.put(adjacentNode, currentNode);
                    if (adjacentNode == sink) {
                        return true;
                    }
                    queue.add(adjacentNode);                    
                    visitedNodes[adjacentNode] = true;              
                }
            }
        }
        
        return false;
    }
}