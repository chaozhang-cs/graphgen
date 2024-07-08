package org.gai.utils;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.generate.GnmRandomBipartiteGraphGenerator;
import org.jgrapht.generate.GnpRandomBipartiteGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
    public static void writeBipartiteVertices(Set<Integer> first, Set<Integer> second, FileWriter writer) throws IOException {
        int firstSize = first.size(), secondSize = second.size();
        writer.write("First partition: ");
        for (Integer v : first) {
            writer.write(v.toString());
            if (--firstSize != 0)
                writer.write(", ");
        }
        writer.write("\n");

        writer.write("Second partition: ");
        for (Integer v : second) {
            writer.write(v.toString());
            if (--secondSize != 0)
                writer.write(", ");
        }
        writer.write("\n");
    }

    public static void createFolders() {
        for (int i = 1; i <= 20; i++) {
            String folderName = String.format("n%02d", i);
            createFolder("./dataset/", folderName);
        }
    }

    public static void createFolder(String path, String folderName) {
        File folder = new File(path, folderName);
        if (!folder.exists()) {
            if (!folder.mkdir())
                System.out.println("Failed to create folder " + folderName);
        } else {
            System.out.println("Folder " + folderName + " already exists.");
        }
    }

    public static void writeGraph(
            File file,
            String generatorName,
            Set<Integer> vertexSet,
            List<Pair<Integer, Integer>> edgeList,
            GraphGenerator<Integer, DefaultEdge, Integer> graphGenerator,
            String info) {
        try (FileWriter writer = new FileWriter(file)) {
            if (generatorName != null)
                writer.write("# " + generatorName + " graphs\n");
            writer.write("# number of vertices: " + vertexSet.size() + "\n");
            writer.write("# number of edges: " + edgeList.size() + "\n");

            writer.write(info);
            writer.write("\n");

            if (graphGenerator != null) {
                // Specifically for the case of bipartite graphs
                // The two partitions are written at the beginning of the CSV files
                if (generatorName.equals("Bipartite-ERP")) {
                    GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge> bg = (GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge>) graphGenerator;
                    writeBipartiteVertices(bg.getFirstPartition(), bg.getSecondPartition(), writer);
                }
                if (generatorName.equals("Bipartite-ERM")) {
                    GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge> bg = (GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge>) graphGenerator;
                    writeBipartiteVertices(bg.getFirstPartition(), bg.getSecondPartition(), writer);
                }
            }

            for (Integer v : vertexSet)
                writer.write(v.toString() + "\n");
            for (Pair<Integer, Integer> e : edgeList)
                writer.write(e.getFirst().toString() + " " + e.getSecond().toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeGraph(
            File file,
            String generatorName,
            Set<Integer> vertexSet,
            List<Pair<Integer, Integer>> edgeList,
            GraphGenerator<Integer, DefaultEdge, Integer> graphGenerator) {
        writeGraph(file, generatorName, vertexSet, edgeList, graphGenerator, null);
    }

    public static void writeGraph(File file, Set<Integer> nodes, List<Pair<Integer, Integer>> edges, String info) {
        writeGraph(file, null, nodes, edges, null, info);
    }

    public static void writeGraph(File file, Pair<Set<Integer>, List<Pair<Integer, Integer>>> g) {
        writeGraph(file, null, g.getFirst(), g.getSecond(), null, null);
    }

    public static Pair<Set<Integer>, List<Pair<Integer, Integer>>> readGraph(String read) {
        List<Pair<Integer, Integer>> edgeList;
        Set<Integer> vertexSet;
        Scanner scanner;
        try {
            scanner = new Scanner(new File(read));
            edgeList = new ArrayList<>();
            vertexSet = new HashSet<>();
            System.out.println("Loading graph: " + read);
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("#"))
                    continue;
                String[] data = line.split(" ");

                if (data.length == 1) { // vertex line
                    vertexSet.add(Integer.parseInt(data[0]));
                } else if (data.length == 2) { // edge line
                    edgeList.add(Pair.of(
                            Integer.parseInt(data[0]),
                            Integer.parseInt(data[1])
                    ));
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return Pair.of(vertexSet, edgeList);
    }

    public static Graph<Integer, DefaultEdge> getGraph(String read) {
        Pair<Set<Integer>, List<Pair<Integer, Integer>>> pair = readGraph(read);
        SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        for (Integer v : pair.getFirst())
            graph.addVertex(v);
        for (Pair<Integer, Integer> e : pair.getSecond())
            graph.addEdge(e.getFirst(), e.getSecond());
        return graph;
    }

    // list all files from this path
    public static List<Path> listFiles(Path path) throws IOException {
        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        return result;
    }

    public static List<String> readPrompts(String folder) {
        List<String> ret = new ArrayList<>();
        List<Path> pathList = null;
        try {
            pathList = listFiles(Path.of(folder));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Path p : pathList) {
            try {
                List<String> lines = Files.readAllLines(p);

                for (String line : lines) {
                    String temp = line.replace(" language <GDL>. ", ".\n<GDL>\nInstruction: ");
                    temp = temp.replace("graph description language", "graph description.\n");
                    ret.add(temp);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("An error occurred while reading the file.");
            }
        }
        return ret;
    }

}
