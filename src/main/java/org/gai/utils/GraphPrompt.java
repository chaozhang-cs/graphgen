package org.gai.utils;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GraphPrompt {
    public static final String PROMPTS_PATH = "./GraphData-main/prompt_generator/top_prompts";
    public static final String[] SERIALIZATIONS = new String[]{
            "Adjacency",
            "FullAdjacency",
            "Incident"
    };
    public static final String SERI_PATH = "./prompt/";
    public static final String GRAPHS_PATH = "./dataset/";

    public static void main(String[] args) {
        generate();
    }

    public static void generate() {
        List<String> prompts = FileUtils.readPrompts(PROMPTS_PATH);
        int i = 0;
        for (Graph<Integer, DefaultEdge> g : getGraphs("./dataset")) {
            int j = 0;
            for (String serialization : SERIALIZATIONS) {
                int k = 0;
                for (String p : prompts) {
                    writePrompt(
                            SERI_PATH + "g-" + i + "-s-" + j + "-p-" + k + ".txt",
                            p.replace(
                                    "<GDL>",
                                    serializeGraph(g, serialization)
                            )
                    );
                    ++k;
                }
                ++j;
            }
            ++i;
        }

    }

    private static List<Graph<Integer, DefaultEdge>> getGraphs(String path) {
        List<Path> pathList = null;
        try {
            pathList = FileUtils.listFiles(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Graph<Integer, DefaultEdge>> graphs = new ArrayList<>();
        for (Path p : pathList)
            graphs.add(FileUtils.getGraph(p.toString()));
        return graphs;
    }

    private static void writePrompt(String name, String content) {
        File file = new File(name);
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String serializeGraph(Graph<Integer, DefaultEdge> graph, String serialization) {
        String ret = null;
        switch (serialization) {
            case "Adjacency":
                ret = getAdjacencyGDL(graph);
                break;
            case "FullAdjacency":
                ret = getFullAdjacencyGDL(graph);
                break;
            case "Incident":
                ret = getIncidentGDL(graph);
                break;
            default:

        }
        return ret;
    }

    private static String getAdjacencyGDL(Graph<Integer, DefaultEdge> g) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nIn the undirected graph, (i,j) means that node i and node j are connected with an undirected edge.\n");
        stringBuilder.append("The graph has the following nodes: ");

        List<Integer> vertexList = new ArrayList<>(g.vertexSet());
        int i = 0;
        for (; i < vertexList.size() - 1; i++)
            stringBuilder.append(vertexList.get(i)).append(", ");
        if (i < vertexList.size())
            stringBuilder.append("and ").append(vertexList.get(i)).append(".\n");

        stringBuilder.append("The edges in the graph are: \n");
        for (DefaultEdge e : g.edgeSet()) {
            stringBuilder.append("\t");
            stringBuilder.append("(").append(g.getEdgeSource(e)).append(", ").append(g.getEdgeTarget(e)).append(")\n");
        }
        return stringBuilder.toString();
    }

    private static String getFullAdjacencyGDL(Graph<Integer, DefaultEdge> g) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\nThe graph has the following nodes: ");
        List<Integer> vertexList = new ArrayList<>(g.vertexSet());
        int i = 0;
        for (; i < vertexList.size() - 1; i++)
            stringBuilder.append("Node ").append(vertexList.get(i)).append(", ");
        if (i < vertexList.size())
            stringBuilder.append("and Node ").append(vertexList.get(i)).append(".\n");

        stringBuilder.append("The edges in the graph are: \n");
        List<DefaultEdge> edgeList = new ArrayList<>(g.edgeSet());
        i = 0;
        for (; i < edgeList.size() - 1; i++)
            stringBuilder.append("\t")
                    .append("Node ").append(g.getEdgeSource(edgeList.get(i))).append(" is connected to Node ")
                    .append(g.getEdgeTarget(edgeList.get(i))).append(" with an edge;\n");

        if (i < edgeList.size())
            stringBuilder
                    .append("\t").append("Node ").append(g.getEdgeSource(edgeList.get(i))).append(" is connected to Node ")
                    .append(g.getEdgeTarget(edgeList.get(i))).append(" with an edge.\n");
        return stringBuilder.toString();
    }

    private static String getIncidentGDL(Graph<Integer, DefaultEdge> g) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nThe graph has the following nodes: ");

        List<Integer> vertexList = new ArrayList<>(g.vertexSet());
        int i = 0;
        for (; i < vertexList.size() - 1; i++)
            stringBuilder.append("Node ").append(vertexList.get(i)).append(", ");
        if (i < vertexList.size())
            stringBuilder.append("and Node ").append(vertexList.get(i)).append(".\n");

        stringBuilder.append("In this graph, \n");

        for (i = 0; i < vertexList.size() - 1; i++) {
            int v = vertexList.get(i);
            List<Integer> neiOfV = getNeighboursOf(g, v);
            if (neiOfV.size() == 1) {
                stringBuilder.append("\t").append("Node ").append(v).append(" has ").append(g.degreeOf(v)).append(" connection: Node ")
                        .append(neiOfV.get(0)).append(".\n");
            } else {
                stringBuilder.append("\t").append("Node ").append(v).append(" has ").append(g.degreeOf(v)).append(" connections: ");
                int j = 0;
                for (; j < neiOfV.size() - 1; j++)
                    stringBuilder.append("Node ").append(neiOfV.get(j)).append(", ");
                if (j < neiOfV.size())
                    stringBuilder.append("and Node ").append(neiOfV.get(j));
                stringBuilder.append(".\n");
            }
        }
        return stringBuilder.toString();
    }

    private static List<Integer> getNeighboursOf(Graph<Integer, DefaultEdge> g, Integer v) {
        List<DefaultEdge> adjOfV = new ArrayList<>(g.edgesOf(v));
        List<Integer> ret = new ArrayList<>();
        for (DefaultEdge e : adjOfV) {
            int source = g.getEdgeSource(e), target = g.getEdgeTarget(e);
            ret.add(v == source ? target : source);
        }
        return ret;
    }
}
