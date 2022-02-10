package networkflow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Stack;
import java.util.stream.Collectors;

import networkflow.algorithms.BreadthFirstSearch;
import networkflow.algorithms.maxflow.FordFulkerson;
import networkflow.core.FlowGraph;

/**
 * Main class, the entrance class!
 *
 */
public class Main
{
    private static final String BFS = "-b", 
                                MAXFLOW = "-f", 
                                CIRCULATIONPROBLEM = "-c";

    public static void main( String[] args )
    {
        if (args.length < 2) {
            System.out.println("Invalid arguments: Use {program} -option input_text_file");
            return;    
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(args[1]))) 
        {            
            List<String[]> lineWiseInput = new ArrayList<>();
            if (List.of(BFS, MAXFLOW, CIRCULATIONPROBLEM).contains(args[0])) {                
                lineWiseInput = ReadFileInput(reader, lineWiseInput);
            }

            Instant starts, ends;
            switch (args[0]) {
                case BFS:
                        if (args.length != 4) {
                            System.out.println("Invalid arguments: Use {program} -b input_text_file source_node destination_node");
                            return;    
                        }                        
                        int source = Integer.parseInt(args[2]);
                        int sink = Integer.parseInt(args[3]);

                        // Creating graph with first node as source and last as sink.
                        FlowGraph graphForBFS = new FlowGraph();
                        graphForBFS.buildGraph(lineWiseInput);
                        Map<Integer, Integer> pathTracker = new HashMap<>();

                        starts = Instant.now();
                        boolean hasReachablePath = BreadthFirstSearch.Run(graphForBFS.getAdjacentList(), source, sink, graphForBFS.getCapacityGraph(), pathTracker);
                        ends = Instant.now();

                        if (hasReachablePath) { 
                            // Tracking nodes which are part of shortest path into a stack and printing them out.
                            Integer[] nodesInShortestPath = Main.TraceBFSTraversalPath(pathTracker, source, sink);
                            String formattedShortestPath = Arrays.stream(nodesInShortestPath)
                                                                 .map(String::valueOf)
                                                                 .collect(Collectors.joining(", "));
                            System.out.println("Shortest path: " + formattedShortestPath);                            
                        } else {
                            System.out.println("Oops! sink cannot be reached from source.");
                        }
                        
                        System.out.println("Duration (in seconds): " + Duration.between(starts, ends).toString().substring(2));
                    break;

                case MAXFLOW:
                        // Creating graph with first node as source and last but one as sink (since zero-based start and empty line for sink node).
                        FlowGraph graphForMaxFlow = new FlowGraph();
                        graphForMaxFlow.buildGraph(lineWiseInput);

                        starts = Instant.now();
                        int maxFlow = FordFulkerson.Run(graphForMaxFlow.getAdjacentList(), graphForMaxFlow.getSource(), graphForMaxFlow.getSink(), graphForMaxFlow.getCapacityGraph());
                        ends = Instant.now();
                        
                        System.out.println("Maximum flow: " + maxFlow);
                        System.out.println("Duration (in seconds): " + Duration.between(starts, ends).toString().substring(2));
                    break;
                        
                case CIRCULATIONPROBLEM:                    
                        // Check if input supply meets demand, else no point in solving.
                        OptionalInt total = GetSumOfSupplyDemandValues(lineWiseInput);
                        if (total.isPresent()) {
                            int totalValue = total.getAsInt();
                            if (totalValue != 0) {
                                String reason = totalValue < 0 ? "supply > demand" : "demand > supply";                      
                                System.out.println("No, it does not have a circulation. For this problem: " + reason);
                            }
                            else {
                                int totalSupply = lineWiseInput.stream().filter(e -> e.length > 0)
                                                      .mapToInt(e -> Integer.parseInt(e[0]))
                                                      .filter(e -> e < 0)
                                                      .reduce((supplyValue, accumulator) -> accumulator + supplyValue)
                                                      .getAsInt();        
                                lineWiseInput = UpdateCirculationGraphInput(lineWiseInput);
    
                                // HANDLE the no out bound edges.
                                FlowGraph graph = new FlowGraph();
                                graph.buildGraph(lineWiseInput);
                                
                                starts = Instant.now();
                                int maxNetFlow = FordFulkerson.Run(graph.getAdjacentList(), graph.getSource(), graph.getSink(), graph.getCapacityGraph());
                                ends = Instant.now();
                                if (maxNetFlow == -totalSupply) { // since total-supply = total-demand, checking with supply here, which is negative.                       
                                    System.out.println("Yes, it has a circulation.");
                                } else {
                                    System.out.println("No, it does not have a circulation. Reason: the minimum cut.");
                                }
    
                                System.out.println("Duration (in seconds): " + Duration.between(starts, ends).toString().substring(2));
                            }
                        } else {
                            System.out.println("Invalid input, please provide valid input.");
                        }
                    break;                        
                default:
                        System.out.println("Invalid input, please follow the pattern: program -option inputfile");
                    break;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reads the input line by line from the buffered reader.
     * @param reader
     * @param lineWiseInput
     * @return
     * @throws Exception
     */
    private static List<String[]> ReadFileInput(BufferedReader reader, List<String[]> lineWiseInput) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            lineWiseInput.add(!line.isEmpty() ? line.split(" ") : new String[0]);
        }
        return lineWiseInput;
    }

    /**
     * Traces the BFS traversal path and returns the nodes in the path in order.
     * @param pathTracker
     * @param sourceNode
     * @param lastVisitedNode
     * @return
     */
    private static Integer[] TraceBFSTraversalPath(Map<Integer, Integer> pathTracker, Integer sourceNode, Integer lastVisitedNode) {
        // tracing the visited nodes by using a stack.
        Stack<Integer> pathTracer = new Stack<Integer>();                            
        do {
            pathTracer.push(lastVisitedNode);
            lastVisitedNode = pathTracker.get(lastVisitedNode);
        } while (lastVisitedNode != sourceNode);
        pathTracer.push(sourceNode);                   
        
        // since the traversal was in reverse, we used stack (LIFO). Now, popping elements into an array and returning it.
        Integer[] visitedNodesInOrder = new Integer[pathTracer.size()];
        int currentIndex = 0;
        while (!pathTracer.isEmpty()) {
            visitedNodesInOrder[currentIndex++] = pathTracer.pop();
        }

        return visitedNodesInOrder;
    }

    /**
     * calculates total of supply and demand values.
     * @param lineWiseInput
     * @return
     */
    private static OptionalInt GetSumOfSupplyDemandValues(List<String[]> lineWiseInput) {
        return lineWiseInput.stream()
                            .filter(e -> e.length > 0)
                            .mapToInt(e -> Integer.parseInt(e[0]))
                            .reduce((supplyOrDemand, accumulator) -> accumulator + supplyOrDemand);        
    }

    /**
     * Updates the circulation graph input.
     * adds the new source and sink nodes from supply and demand nodes respectively.
     * for more info, check the circulation problem description. 
     * @param lineWiseInput
     * @param totalSupply
     * @return
     */
    private static List<String[]> UpdateCirculationGraphInput(List<String[]> lineWiseInput) {        
        ArrayList<CirculationDetail> circulationDetails = new ArrayList<>();
        Map<Integer, List<String>> tempGraphInput = new LinkedHashMap<Integer, List<String>>();

        int linesCount = lineWiseInput.size(), source = 0, sink = linesCount + 1; // +1, since source will be added as line-0

        /**
         * for each input line splices the first token which is supply/demand
         * and stores the removed value and its details as new object in another list, 
         * in order to add it back to source/sink node to solve the circulation problem.
         */
        for (int lineNumber = 0; lineNumber < linesCount; lineNumber++) {
            String[] tokens = lineWiseInput.get(lineNumber);

            int firstToken = tokens.length > 0 ? Integer.parseInt(tokens[0]) : 0; // supply or demand value.
            if (firstToken > 0) {
                circulationDetails.add(new CirculationDetail(lineNumber + 1, sink, firstToken));
            } else if (firstToken < 0) {
                circulationDetails.add(new CirculationDetail(source, lineNumber + 1, -firstToken));
            }
            
            String[] firstTokenTrimmedList = tokens.length == 0 ? new String[0] : Arrays.copyOfRange(tokens, 1, tokens.length); // removing supply/demand value, ie. first element in line.
            
            /** 
             * since nodes are being started from zero in input-graph, we have to increment the node values 
             * as we are following zero-based node as id and adding new source node as first line.
             */ 
            firstTokenTrimmedList = IncrementNodeNumberInInput(firstTokenTrimmedList); 
            List<String> trimmedTokenList = Arrays.stream(firstTokenTrimmedList)                                                
                                                  .collect(Collectors.toList());
            tempGraphInput.put(lineNumber+1, trimmedTokenList);
        }

        /**
         * adding the temp-stored circulation-details list, supply/demand node to new source/sink node data updates.
         * updating the new-source data into a new string[] which we are adding to final updated list at the end.
         * and updating the new-sink node data from every existing demand node by updating existing node datas disctionary/map. 
         */
        List<String> newSourceNodeData = new ArrayList<String>();
        for (CirculationDetail circulation : circulationDetails) {
            if (circulation.fromNode == source) { // supply details, hence, to be added in source/first input line.
                newSourceNodeData.add(String.valueOf(circulation.toNode)); // adding adjacent-node
                newSourceNodeData.add(String.valueOf(circulation.value)); // adding edge-weight
            } else { // demand details
                int newLineNumber = circulation.fromNode;
                List<String> existingStrings = tempGraphInput.get(newLineNumber);
                existingStrings.add(String.valueOf(circulation.toNode)); // toNode/sink, adding adjacent-node
                existingStrings.add(String.valueOf(circulation.value)); // adding edge-weight
                tempGraphInput.put(newLineNumber, existingStrings);
            }
        }

        List<String[]> updatedGraphInput = new ArrayList<>();

        // adding the new source node data to input-graph-data.
        updatedGraphInput.add(newSourceNodeData.stream().toArray(String[]::new));
        for (int i = 1; i <= linesCount; i++) {
            updatedGraphInput.add(tempGraphInput.get(i).stream().toArray(String[]::new));
        }
        return updatedGraphInput;
    }

    /**
     * Increments the node/vertex id by one since we are adding 
     * new source node of zero, which will collide with given input format.
     * @param graphLineInput
     * @return
     */
    private static String[] IncrementNodeNumberInInput(String[] graphLineInput) {
        String[] updatedInput = new String[graphLineInput.length];
        for (int i = 0; i < graphLineInput.length; i++ ) {
            String newValue = graphLineInput[i];
            if (i % 2 == 0) { // as we removed supply/demand value, every even position number is a node.
                int nodeValue = Integer.parseInt(newValue);
                newValue = String.valueOf(++nodeValue);
            }
            updatedInput[i] = newValue;
        }

        return updatedInput;
    }

    /**
     * Circulation detail class for details of supply/demand with from/to nodes.
     */
    private static class CirculationDetail {
        public int fromNode;
        public int toNode;
        public int value;

        public CirculationDetail(int from, int to, int value) {
            this.fromNode = from;
            this.toNode = to;
            this.value = value;
        }
    }
}