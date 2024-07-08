package org.gai.utils;


import org.gai.generate.MyPathGraphGenerator;
import org.gai.generate.MyStarGraphGenerator;
import org.jgrapht.Graph;
import org.jgrapht.generate.*;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.function.Supplier;

import static org.gai.utils.FileUtils.*;
import static org.gai.utils.GraphUtils.getEdgeList;


public class GraphGenUtil {

    public static final int NUM_ERM_GRAPHS = 50_000;
    public static final int NUM_ERP_GRAPHS = 50_000;
    public static final int NUM_BA_GRAPHS = 50_000;
    public static final int NUM_BA_FORESTS = 50_000;
    public static final int NUM_SF_GRAPHS = 50_000;
    public static final int NUM_SBM_GRAPHS = 50_000;
    public static final int NUM_BP_ERM_GRAPHS = 50_000;
    public static final int NUM_BP_ERP_GRAPHS = 50_000;


    public static void generate(){
        createFolders();
        for (int i = 1; i <= 6; i++)
            generateAllGraphsFromOneToSix(i);
        for (int n = 7; n <= 20; n++) {
            int finalN = n;

            generateGraphsFromSevenToTwenty(
                    n,
                    () -> new EmptyGraphGenerator<>(finalN),
                    "EG"
            );

            generateGraphsFromSevenToTwenty(
                    n,
                    () -> {
                        Random random = new Random(System.nanoTime());
                        int m = random.nextInt(finalN % 2 == 0 ? Math.multiplyExact(finalN / 2, finalN - 1) : Math.multiplyExact((finalN - 1) / 2, finalN)) + 1;
                        return new GnmRandomGraphGenerator<>(finalN, m, System.nanoTime());
                    },
                    "ERM"
            );

            generateGraphsFromSevenToTwenty(
                    n,
                    () -> {
                        Random random = new Random(System.nanoTime());
                        double p = random.nextDouble();
                        while (p == 0.0)
                            p = random.nextDouble();
                        return new GnpRandomGraphGenerator<>(finalN, p, System.nanoTime());
                    },
                    "ERP"
            );

            generateGraphsFromSevenToTwenty(
                    n,
                    () -> {
                        Random random = new Random(System.nanoTime());
                        int m0 = random.nextInt(finalN / 3) + 1;
                        while (m0 == 1)
                            m0 = random.nextInt(finalN / 3) + 1;
                        int m = random.nextInt(m0) + 1;
                        return new BarabasiAlbertGraphGenerator<>(m0, m, finalN, System.nanoTime());
                    },
                    "BAG"
            );

            generateGraphsFromSevenToTwenty(
                    n,
                    () -> {
                        Random random = new Random(System.nanoTime());
                        int t = random.nextInt(finalN / 2) + 1;
                        return new BarabasiAlbertForestGenerator<>(t, finalN, System.nanoTime());
                    },
                    "BAF"
            );

            generateGraphsFromSevenToTwenty(
                    n,
                    () -> new ScaleFreeGraphGenerator<>(finalN, System.nanoTime()),
                    "SF"
            );

            generateGraphsFromSevenToTwenty(
                    n,
                    () -> {
                        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
                        for (int i = 0; i < finalN; i++)
                            g.addVertex(i);
                        return new ComplementGraphGenerator<>(g);
                    },
                    "Complete"
            );

            generateGraphsFromSevenToTwenty(
                    n,
                    () -> {
                        Random random = new Random(System.nanoTime());
                        int n1 = random.nextInt(finalN) + 1;
                        while (n1 == finalN)
                            n1 = random.nextInt(finalN) + 1;
                        int n2 = finalN - n1;
                        int m = random.nextInt(n1 * n2) + 1;
                        return new GnmRandomBipartiteGraphGenerator<>(n1, n2, m, System.nanoTime());
                    },
                    "Bipartite-ERM"
            );

            generateGraphsFromSevenToTwenty(
                    n,
                    () -> {
                        Random random = new Random(System.nanoTime());
                        int n1 = random.nextInt(finalN) + 1;
                        while (n1 == finalN)
                            n1 = random.nextInt(finalN) + 1;
                        int n2 = finalN - n1;
                        double p = random.nextDouble();
                        while (p == 0.0)
                            p = random.nextDouble();
                        return new GnpRandomBipartiteGraphGenerator<>(n1, n2, p, System.nanoTime());
                    },
                    "Bipartite-ERP"
            );

            generateGraphsFromSevenToTwenty(
                    n,
                    () -> new MyStarGraphGenerator(finalN, new Random(System.nanoTime()).nextInt(finalN)),
                    "Star"
            );

            generateGraphsFromSevenToTwenty(
                    n,
                    () -> new MyPathGraphGenerator(finalN, new Random(System.nanoTime())),
                    "Path"
            );
        }
    }

    private static int getNumOfGraphs(String graphGenName, Integer numOfNodes) {
        int ret;
        switch (graphGenName) {
            case "EG":
            case "Complete":
                ret = 1;
                break;
            case "ERP":
                ret = NUM_ERP_GRAPHS;
                break;
            case "ERM":
                ret = NUM_ERM_GRAPHS;
                break;
            case "BAG":
                ret = NUM_BA_GRAPHS;
                break;
            case "BAF":
                ret = NUM_BA_FORESTS;
                break;
            case "SF":
                ret = NUM_SF_GRAPHS;
                break;
            case "Path":
                ret = numOfNodes * (numOfNodes - 1) / 2;
                break;
            case "Star":
                ret = numOfNodes;
                break;
            case "SBM":
                ret = NUM_SBM_GRAPHS;
                break;
            case "Bipartite-ERP":
                ret = NUM_BP_ERP_GRAPHS;
                break;
            case "Bipartite-ERM":
                ret = NUM_BP_ERM_GRAPHS;
                break;
            default:
                ret = 0;
        }
        return ret;
    }

    public static void generateAllGraphsFromOneToSix(int n) {
        int totalGraphs = (int) Math.pow(2, (double) (n * (n - 1)) / 2);

        int[][] combinations = generateCombinations(n);

        for (int i = 0; i < totalGraphs; i++) {
            String numNodes = String.format("n%02d", n);
            String graphId = String.format("graph_%06d", i + 1);

            String fileName = graphId + ".csv";

            // create folder ./dataset/nxx/graphxxxxxx/
            createFolder("./dataset/" + numNodes + "/", graphId);

            File file = new File("./dataset/" + numNodes + "/" + graphId, fileName);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write("# number of vertices: " + n + "\n");
                writer.write("# number of edges: " + Integer.bitCount(i) + "\n");
                for (int k = 0; k < n; k++)
                    writer.write(k + "\n");
                for (int j = 0; j < combinations.length; j++) {
                    if ((i & (1 << j)) != 0) {
                        writer.write(combinations[j][0] + " " + combinations[j][1] + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int[][] generateCombinations(int nodes) {
        int edgeCount = (nodes * (nodes - 1)) / 2;
        int[][] combinations = new int[edgeCount][2];
        int index = 0;
        for (int i = 0; i < nodes; i++) {
            for (int j = i + 1; j < nodes; j++) {
                combinations[index][0] = i;
                combinations[index][1] = j;
                index++;
            }
        }
        return combinations;
    }

    public static void generateGraphsFromSevenToTwenty(
            int nodes,
            Supplier<GraphGenerator<Integer, DefaultEdge, Integer>> graphGeneratorSupplier,
            String generatorName) {

        System.out.println("number of vertices: " + nodes);
        System.out.println("generation methods: " + generatorName);

        String numNodes = String.format("n%02d", nodes);
        createFolder("./dataset/" + numNodes, generatorName);

        int numOfGraphs = getNumOfGraphs(generatorName, nodes);

        for (int i = 0; i < numOfGraphs; i++) {
            String graphId = String.format("graph_%06d", i + 1);
            String fileName = graphId + ".csv";
            // create folder ./dataset/nxx/graphxxxxxx/
            createFolder("./dataset/" + numNodes + "/" + generatorName, graphId);
            File file = new File("./dataset/" + numNodes + "/" + generatorName + "/" + graphId, fileName);

            Graph<Integer, DefaultEdge> g = GraphTypeBuilder
                    .<Integer, DefaultEdge>undirected()
                    .allowingMultipleEdges(false)
                    .allowingSelfLoops(false)
                    .edgeClass(DefaultEdge.class)
                    .weighted(false)
                    .vertexSupplier(SupplierUtil.createIntegerSupplier())
                    .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier())
                    .buildGraph();

            GraphGenerator<Integer, DefaultEdge, Integer> graphGenerator = graphGeneratorSupplier.get();

            graphGenerator.generateGraph(g);

            writeGraph(file, generatorName, g.vertexSet(), getEdgeList(g), graphGenerator);
//            try (FileWriter writer = new FileWriter(file)) {
//                writer.write("# " + generatorName + " graphs\n");
//                writer.write("# number of vertices: " + g.vertexSet().size() + "\n");
//                writer.write("# number of edges: " + g.edgeSet().size() + "\n");
//
//                // Specifically for the case of bipartite graphs
//                // The two partitions are written at the beginning of the CSV files
//                if (generatorName.equals("Bipartite-ERP")) {
//                    GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge> bg = (GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge>) graphGenerator;
//                    writeBipartiteVertices(bg.getFirstPartition(), bg.getSecondPartition(), writer);
//                }
//                if (generatorName.equals("Bipartite-ERM")) {
//                    GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge> bg = (GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge>) graphGenerator;
//                    writeBipartiteVertices(bg.getFirstPartition(), bg.getSecondPartition(), writer);
//                }
//
//                for (Integer v : g.vertexSet())
//                    writer.write(v + "\n");
//                for (DefaultEdge e : g.edgeSet())
//                    writer.write(g.getEdgeSource(e) + " " + g.getEdgeTarget(e) + "\n");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }
}