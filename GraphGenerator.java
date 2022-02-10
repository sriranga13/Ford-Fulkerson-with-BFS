package networkflow.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import networkflow.Constants;


/**
 * Class to generate graphs with random number of nodes and edges.
 */
public class GraphGenerator {

    private static String BasePath = System.getProperty("user.dir");

    private static Random random = new Random();

    /**
     * main method, or entrance point of the program.
     * @param args
     */
    public static void main(String... args) {
        args = new String[] { "896", "903", "1" };
        if (args.length != 3) {
            System.out.println("Invalid input, use syntax: program.exe min_nodes max_nodes no_of_times_to_repeat");
            return;
        }

        int startFrom = Integer.parseInt(args[0]), 
            endBy = Integer.parseInt(args[1]), 
            repeatUntil = Integer.parseInt(args[2]);
        if (startFrom >= endBy) {
            System.out.println("Invalid input, min_nodes(first argument) <= max_nodes (second argument)"); return;
        } 
        
        GraphGenerator.GenerateGraphs(startFrom, endBy, Math.max(repeatUntil, 1));
    }

    /**
     * Generate the graphs in given range for given number of times.
     * @param startFrom
     * @param endBy
     * @param repeatUntil
     */
    private static void GenerateGraphs(int startFrom, int endBy, int repeatUntil) 
    {
        long startTime = GraphGenerator.GetTimeInMilliSeconds();

        while (repeatUntil-- > 0) {
            // it is better to have nodes minimum of 4 for a better graph.
            for (int n = startFrom; n <= endBy; n++) {
                /** 
                 *  From graph theory, a simple graph is a graph which can only have edges 
                 *  without self, or parallel edges.
                 *  -> min edges: n-1 (otherwise, graph will not be a connected graph)
                 *  -> max edges: nC2, or n*(n-1)/2.
                 */
                int minEdges = n - 1, maxEdges = (n * minEdges) / 2; 
                int numberOfEdges = GetRandomNumber(minEdges, maxEdges);
                List<String> graphData = GraphGenerator.CreateGraph(n, numberOfEdges);

                String fileName = GraphGenerator.GetFileName(n, startTime); // generating filename.
                GraphGenerator.CreateFile(fileName, graphData); // generating file.
            }
        }
    }

    /**
     * Creates the graph with given number of nodes and edges.
     * @param numberOfNodes
     * @param numberOfEdges
     * @return The graph data generated.
     */
    private static List<String> CreateGraph(int numberOfNodes, int numberOfEdges) {
        int source = GetRandomNumber(0, numberOfNodes);
        int sink = GetRandomSinkNode(source, numberOfNodes);
        int[][] edgeWeights = InitializeEdgeCapacities(numberOfNodes);

        for (int fromNode = 0; fromNode < numberOfNodes; fromNode++) {
            if (fromNode == sink) { // since, sink will not have outward edges from.
                continue;
            }

            // int edgeCounter = 0, 
            int edgesForNode = GetRandomNumber(numberOfNodes - 1, numberOfEdges) + (int)(numberOfNodes * 0.5);
            
            while (edgesForNode-- > 0) {
                int toNode; // creates to-node by validating with rules of simple and connected graph.
                do {
                    toNode = GetRandomNumber(0, numberOfNodes);
                } 
                while (fromNode != toNode && toNode != source && !(edgeWeights[fromNode][toNode] > 0 || edgeWeights[toNode][fromNode] > 0));

                // generating capacity in-between 1 to 100.                    
                int capacity = GetRandomNumber(1, 30);
                edgeWeights[fromNode][toNode] = capacity;

                // edgeCounter++;
            }

            // if (edgeCounter >= numberOfEdges) break;
        }

        return GraphGenerator.GetNodeWiseData(numberOfNodes, edgeWeights);
    }

    private static List<String> GetNodeWiseData(int numberOfNodes, int[][] edgeWeights) {
        List<String> graphData = new ArrayList<String>(numberOfNodes);
        String graphNodeData = new String();
        for (int u = 0; u <= numberOfNodes; u++) {
            graphNodeData = "";
            for (int v = 0; v <= numberOfNodes; v++) {
                if (u != v && edgeWeights[u][v] != Constants.NO_EDGE) {
                    graphNodeData += (graphNodeData.length() > 0 ? " " : "") + v + " " + edgeWeights[u][v];
                }
            }
            graphData.add(graphNodeData);
        }
        return graphData;
    }

    /**
     * Initialize the edge weights.
     * @param numberOfNodes
     * @return
     */
    private static int[][] InitializeEdgeCapacities(int numberOfNodes) {
        int[][] edgeWeights = new int[numberOfNodes + 1][numberOfNodes + 1];
        for (int u = 0; u <= numberOfNodes; u++) {
            for (int v = u; v <= numberOfNodes; v++) {
                edgeWeights[u][v] = Constants.NO_EDGE;
                edgeWeights[v][u] = Constants.NO_EDGE;                
            }
        }
        return edgeWeights;
    }

    /**
     * Gets the random sink node.
     * @param source
     * @param numberOfNodes
     * @return
     */
    private static int GetRandomSinkNode(int source, int numberOfNodes) {
        int sink;
        do {
            sink = GetRandomNumber(0, numberOfNodes);
        } while (sink == source);
        
        return sink;
    }

    /**
     * Creates a file with file-name and contents passed.
     * @param fileName
     * @param graphData
     */
    private static void CreateFile(String fileName, List<String> graphData) {
        try {
            Path file = Paths.get(BasePath + "/fileinput/" + fileName);
            Files.write(file, graphData, Charset.defaultCharset());
            System.out.println(fileName);
        } catch (IOException e) {
            System.out.println("An error occurred while creating a file.");
            e.printStackTrace();
        }
    }

    /**
     * Gets the time in milliseconds.
     * @return the time.
     */
    private static long GetTimeInMilliSeconds() {
        return System.currentTimeMillis(); // currentTime in milli-seconds.
    }

    /**
     * Gets the file name.
     * @param startTime
     * @param nodeCount
     * @return
     */
    private static String GetFileName(int nodeCount, long startTime) {
        return startTime + "_" + "graph" + "_" + nodeCount + ".txt";
    }

    /**
     * Generates the random number within min and max values.
     * @param min
     * @param max
     * @return the random number in range.
     */
    private static int GetRandomNumber(int min, int max) {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        // return ThreadLocalRandom.current().nextInt(min, max + 1);
        return random.nextInt((max - min) + 1) + min;
    }
}
