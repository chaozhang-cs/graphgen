package org.gai.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jgrapht.GraphMetrics;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.jgrapht.util.SupplierUtil;

import java.util.*;
import java.util.function.Function;

public class GraphProperties {

    public static final String
            N_C = "node_count",
            E_C = "edge_count",
            E_A = "edge_absence",
            NUM_CC = "number_of_connected_components",
            CC = "connected_components",
            CC_T = "connectivity_true",
            CC_F = "connectivity_false",
            S_T = "spanning_tree",
            BFS_O = "bfs_traversal_order",
            DFS_O = "dfs_traversal_order",
            CYC_C = "cycle_check",
            TOPO_O = "topological_sort_order",
            DIAMETER = "diameter",
            RADIUS = "radius",
            GIRTH = "girth",
            NUM_TRI = "number_of_triangles",
            S_P = "shortest_path",
            MAX_FLOW = "maximum_flow",
            HAMILTON_P = "hamilton_path";


    public static final Set<String> PROPERTIES = Set.of(
            N_C,
            E_C,
            E_A,
            NUM_CC,
            CC,
            CC_T,
            CC_F,
            S_T,
            BFS_O,
            DFS_O,
            CYC_C,
            TOPO_O,
            DIAMETER,
            RADIUS,
            GIRTH,
            NUM_TRI,
            S_P,
            MAX_FLOW,
            HAMILTON_P
    );

    private static final Random RAN = new Random(System.nanoTime());

    private final SimpleGraph<Integer, DefaultEdge> g; // undirected, unweighted, no loop, no multiple edges
    private final JsonObject graphProperties;


    public GraphProperties(SimpleGraph<Integer, DefaultEdge> g, JsonObject graphProperties) {
        this.g = g;
        this.graphProperties = graphProperties;
    }

    /**
     * node_count : number of nodes
     */
    public void addNodeCount() {
        int nodeCount = g.vertexSet().size();
        graphProperties.addProperty(N_C, nodeCount);
    }

    /**
     * edge_count : number of edges
     */
    public void addEdgeCount() {
        int edgeCount = g.edgeSet().size();
        graphProperties.addProperty(E_C, edgeCount);
    }

    /**
     * edge_absence : an array of edges that do not exist in the graph
     * array size is |E| / 2
     * each element in the array is a string with the form "(u,v)"
     */
    public void addEdgeAbsence() {
        List<Integer> vertices = new ArrayList<>(g.vertexSet());
        Set<DefaultEdge> edgeSet = g.edgeSet();

        JsonArray pairsArray = new JsonArray();

        if (!edgeSet.isEmpty()) {
            Random random = new Random(System.nanoTime());
            int numEdges = edgeSet.size();
            Set<Pair<Integer, Integer>> property = new HashSet<>();
            while (property.size() < numEdges / 2) {
                int u = vertices.get(random.nextInt(vertices.size()));
                int v = vertices.get(random.nextInt(vertices.size()));
                if (u == v || g.containsEdge(u, v))
                    continue;
                property.add(Pair.of(u, v));
            }

            for (Pair<Integer, Integer> e : property)
                addVertexPairs(pairsArray, e);
        }
        graphProperties.add(E_A, pairsArray);
    }

    /**
     * number_of_connected_components : the number of connected component in the graph
     * connected_components : an array of elements with each representing a connected component in the form of connected_component: an array of vertices
     * connectivity_true : an array of vertex pairs (u,v), such that each pair of vertices belong to the same connected component
     * connectivity_false : an array of vertex pairs (u,v), such that each pair of vertices do not belong to the same connected component
     */
    public void addConnectivityInfo() {
        ConnectivityInspector<Integer, DefaultEdge> connectivityInspector = new ConnectivityInspector<>(g);
        List<Set<Integer>> connectedComponents = connectivityInspector.connectedSets();

        // add number_of_connected_components
        int numOfCC = connectedComponents.size();
        graphProperties.addProperty(NUM_CC, numOfCC);

        // add connected_components
        JsonArray arrayCC = new JsonArray();
        for (Set<Integer> cc : connectedComponents) {
            JsonArray jsonElements = new JsonArray(cc.size());
            for (Integer v : cc)
                jsonElements.add(v);
            arrayCC.add(jsonElements);
        }
        graphProperties.add(CC, arrayCC);


        // add connectivity
        Set<Integer> vertexSet = g.vertexSet();
        int numOfQueries = vertexSet.size() / 2;

        List<Pair<Integer, Integer>> trueQueries = new ArrayList<>(), falseQueries = new ArrayList<>();
        List<Integer> vertexList = new ArrayList<>(vertexSet);

        List<List<Integer>> cCList = new ArrayList<>();
        for (Set<Integer> cc : connectedComponents)
            cCList.add(new ArrayList<>(cc));

        if (numOfCC == 1) { // all vertices are connected
            while (trueQueries.size() < numOfQueries) {
                int u = getRandElem(vertexList), v = getRandElem(vertexList);
                if (u != v)
                    trueQueries.add(Pair.of(u, v));
            }
        } else if (numOfCC == vertexSet.size()) {// all vertices are not connected
            while (falseQueries.size() < numOfQueries) {
                int u = getRandElem(vertexList), v = getRandElem(vertexList);
                if (u != v)
                    falseQueries.add(Pair.of(u, v));
            }
        } else {

            while (trueQueries.size() < numOfQueries) {
                List<Integer> cc = getRandElem(cCList);
                int u = getRandElem(cc), v = getRandElem(cc);
                if (u != v)
                    trueQueries.add(Pair.of(u, v));
            }

            while (falseQueries.size() < numOfQueries) {
                List<Integer> cc1 = getRandElem(cCList), cc2 = getRandElem(cCList);
                if (cc1 == cc2)
                    continue;

                int uInCC1 = getRandElem(cc1), uInCC2 = getRandElem(cc2);
                falseQueries.add(Pair.of(uInCC1, uInCC2));
            }
        }

        JsonArray tQ = new JsonArray(trueQueries.size()), fQ = new JsonArray(falseQueries.size());
        for (Pair<Integer, Integer> q : trueQueries)
            addVertexPairs(tQ, q);
        for (Pair<Integer, Integer> q : falseQueries)
            addVertexPairs(fQ, q);

        graphProperties.add("connectivity_true", tQ);
        graphProperties.add("connectivity_false", fQ);

    }

    /**
     * spanning_tree : an array of vertex pairs, where each pair represent an edge in the spanning tree;
     * if the graph has more than one connected component, then the graph has a spanning forest, instead of a spanning tree
     */
    public void addSpanningTree() {
        // adding spanning tree
        JsonArray spanningTreeEdges = new JsonArray();

        SimpleGraph<Integer, DefaultEdge> weightedG = transToWeightedG();

        SpanningTreeAlgorithm.SpanningTree<DefaultEdge> st = new KruskalMinimumSpanningTree<>(weightedG).getSpanningTree(); // g is a simple graph with the default edge weigh of 1.0
        Set<DefaultEdge> edges = st.getEdges();
        for (DefaultEdge e : edges) {
            int u = g.getEdgeSource(e);
            int v = g.getEdgeTarget(e);
            spanningTreeEdges.add("(" + u + "," + v + ")");
        }

        graphProperties.add(S_T, spanningTreeEdges);
    }

    /**
     * bfs_traversal_order : an array of pairs (start, order)
     * start stores the starting vertex of the bfs traversal
     * order stores the sequence of vertices that were visited during the traversal
     */
    public void addBFSOrder() {
        addTraversalOrder(v -> new BreadthFirstIterator<>(g, v), BFS_O);
    }

    /**
     * dfs_traversal_order : an array of pairs (start, order)
     * start stores the starting vertex of the dfs traversal
     * order stores the sequence of vertices that were visited during the traversal
     */
    public void addDFSOrder() {
        addTraversalOrder(v -> new DepthFirstIterator<>(g, v), DFS_O);
    }

    private void addTraversalOrder(Function<Integer, Iterator<Integer>> getIte, String propertyName) {
        List<Integer> verList = new ArrayList<>(g.vertexSet());

        int numOfStartVertices = verList.size() / 4;

        if (numOfStartVertices == 0)
            numOfStartVertices = 1;

        List<Integer> sV = new ArrayList<>();
        for (int i = 0; i < numOfStartVertices; i++)
            sV.add(getRandElem(verList));

        JsonArray jsonElements = new JsonArray(numOfStartVertices);
        for (Integer v : sV) {
            String s = getVertexSeq(getIte.apply(v));

            JsonObject object = new JsonObject();
            object.addProperty("start", v);
            object.addProperty("order", s.toString());
            jsonElements.add(object);
        }
        graphProperties.add(propertyName, jsonElements);
    }

    /**
     * cycle_check : boolean, indicating whether the graph has a cycle
     */
    public void addCycle() {
        CycleDetector<Integer, DefaultEdge> cycleDetector = new CycleDetector<>(g);
        graphProperties.addProperty(CYC_C, cycleDetector.detectCycles());
    }

    /**
     * topological_sort_order : a sequence of vertices, representing a topological order
     * notice that the undirected graph is translated into a directed graph for the computation, where each undirected edge (u,v) is treated a directed edge (u,v) from u to v
     */
    public void addTopologicalSort() {
        CycleDetector<Integer, DefaultEdge> cycleDetector = new CycleDetector<>(g);
        if (cycleDetector.detectCycles())
            graphProperties.addProperty(CYC_C, ""); // g has cycles, such that the topological sort is empty
        else {
            SimpleDirectedGraph<Integer, DefaultEdge> directedGraph = transToDirectedG();
            graphProperties.addProperty(TOPO_O, getVertexSeq(new TopologicalOrderIterator<>(directedGraph)));
        }
    }

    /**
     * diameter : d, an integer indicating the longest shortest path on the graph
     * if the graph is disconnected, d is infinite, denoted as inf
     */
    public void addDiameter() {
        double d = GraphMetrics.getDiameter(transToWeightedG());
        if (d == Double.POSITIVE_INFINITY)
            graphProperties.addProperty(DIAMETER, "inf");
        else
            graphProperties.addProperty(DIAMETER, d);
    }

    /**
     * radius : d, an integer indicate the radium on the graph
     * if the graph is disconnected, d is infinite, denoted as inf
     */
    public void addRadius() {
        double d = GraphMetrics.getRadius(transToWeightedG());
        if (d == Double.POSITIVE_INFINITY)
            graphProperties.addProperty(RADIUS, "inf");
        else
            graphProperties.addProperty(RADIUS, d);
    }

    /**
     * girth : l, the length (number of edges) of the smallest cycle in the graph
     * if the graph is acyclic, l is infinite, denoted as inf
     */
    public void addGirth() {
        int l = GraphMetrics.getGirth(g);
        if (l == Integer.MAX_VALUE)
            graphProperties.addProperty(GIRTH, "inf");
        else
            graphProperties.addProperty(GIRTH, l);
    }


    /**
     * number_of_triangles : the number of triangles in the graph
     */
    public void addNumTriangles() {
        graphProperties.addProperty(NUM_TRI, GraphMetrics.getNumberOfTriangles(g));
    }

    /**
     * shortest_path :
     */
    public void addShortestPath() { // todo:


    }

    private String getVertexSeq(Iterator<Integer> ite) {
        StringBuilder s = new StringBuilder();
        s.append("(");
        while (ite.hasNext())
            s.append(ite.next()).append(",");

        if (s.length() == 1)
            s.deleteCharAt(0);
        else
            s.deleteCharAt(s.length() - 1).append(")");

        return s.toString();
    }

    private void addVertexPairs(JsonArray jsonElements, Pair<Integer, Integer> vertexPair) {
//        JsonObject pair = new JsonObject();
//        pair.addProperty("first", vertexPair.getFirst());
//        pair.addProperty("second", vertexPair.getSecond());
//        jsonElements.add(pair);
        jsonElements.add("(" + vertexPair.getFirst() + "," + vertexPair.getSecond() + ")");
    }

    private SimpleGraph<Integer, DefaultEdge> transToWeightedG() {
        SimpleGraph<Integer, DefaultEdge> weightedG = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), true);
        for (int v : g.vertexSet())
            weightedG.addVertex(v);
        for (DefaultEdge e : g.edgeSet())
            weightedG.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e));
        return weightedG;
    }

    private SimpleDirectedGraph<Integer, DefaultEdge> transToDirectedG() {
        SimpleDirectedGraph<Integer, DefaultEdge> directedGraph = new SimpleDirectedGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        for (Integer v : g.vertexSet())
            directedGraph.addVertex(v);
        for (DefaultEdge e : g.edgeSet())
            directedGraph.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e));
        return directedGraph;
    }

    private static <T> T getRandElem(List<T> list) {
        return list.get(RAN.nextInt(list.size()));
    }

}
