package org.gai.utils;


import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.gai.utils.FileUtils.*;

public class DataAugUtils {

    public static void augment() {
        List<Path> totalFiles;
        try {
            totalFiles = listFiles(Paths.get("./dataset"));
            System.out.println("Total number of files: " + totalFiles.size());

            for (Path p : totalFiles) {
                String parent = p.getParent().toString();
                String fileName = p.getFileName().toString();

                int pos = fileName.lastIndexOf(".");
                String graphName = fileName.substring(0, pos);

                Pair<Set<Integer>, List<Pair<Integer, Integer>>> g = readGraph(p.toString());
                Triple<Set<Integer>, List<Pair<Integer, Integer>>, List<Integer>> gNodeShift1 = nodeShifting(g);
                Triple<Set<Integer>, List<Pair<Integer, Integer>>, List<Integer>> gNodeShift2 = nodeShifting(g);

                while (gNodeShift2.getThird().equals(gNodeShift1.getThird()))
                    gNodeShift2 = nodeShifting(g);

                writeGraph(new File(parent, graphName + "-node-shift-1.csv"), gNodeShift1.getFirst(), gNodeShift1.getSecond(), nodeShiftingSer(gNodeShift1.getThird()));
                writeGraph(new File(parent, graphName + "-node-shift-2.csv"), gNodeShift2.getFirst(), gNodeShift2.getSecond(), nodeShiftingSer(gNodeShift2.getThird()));

                Pair<Set<Integer>, List<Pair<Integer, Integer>>> gES = edgeShifting(g);
                writeGraph(new File(parent, graphName + "-edge-shift-1.csv"), gES);

                Pair<Set<Integer>, List<Pair<Integer, Integer>>> nS1ES1 = edgeShifting(Pair.of(gNodeShift1.getFirst(), gNodeShift1.getSecond()));
                while (nS1ES1.getSecond().equals(gNodeShift1.getSecond())) // todo:
                    nS1ES1 = edgeShifting(Pair.of(gNodeShift1.getFirst(), gNodeShift1.getSecond()));
                writeGraph(new File(parent, graphName + "-node-shift-1-edge-shift-1.csv"), nS1ES1.getFirst(), nS1ES1.getSecond(), nodeShiftingSer(gNodeShift1.getThird()));

                Pair<Set<Integer>, List<Pair<Integer, Integer>>> nS2ES1 = edgeShifting(Pair.of(gNodeShift2.getFirst(), gNodeShift2.getSecond()));
                while (nS2ES1.getSecond().equals(gNodeShift2.getSecond())) // todo:
                    nS2ES1 = edgeShifting(Pair.of(gNodeShift2.getFirst(), gNodeShift2.getSecond()));
                writeGraph(new File(parent, graphName + "-node-shift-2-edge-shift-1.csv"), nS2ES1.getFirst(), nS2ES1.getSecond(), nodeShiftingSer(gNodeShift2.getThird()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Triple<Set<Integer>, List<Pair<Integer, Integer>>, List<Integer>> nodeShifting(Pair<Set<Integer>, List<Pair<Integer, Integer>>> originalG) {
        Set<Integer> originalNodes = originalG.getFirst();
        List<Pair<Integer, Integer>> originalEdges = originalG.getSecond();

        List<Integer> original2New = IntStream.range(0, originalNodes.size()).boxed().collect(Collectors.toList());
        Collections.shuffle(original2New, new Random(System.nanoTime()));
        Set<Integer> newNodes = new HashSet<>(original2New);

        List<Pair<Integer, Integer>> newEdges = new ArrayList<>();
        for (Pair<Integer, Integer> e : originalEdges)
            newEdges.add(Pair.of(
                    original2New.get(e.getFirst()),
                    original2New.get(e.getSecond())
            ));

        return Triple.of(newNodes, newEdges, original2New);
    }

    public static Pair<Set<Integer>, List<Pair<Integer, Integer>>> edgeShifting(Pair<Set<Integer>, List<Pair<Integer, Integer>>> originalG) {
        Set<Integer> originalNodes = originalG.getFirst();
        List<Pair<Integer, Integer>> originalEdges = originalG.getSecond();
        Set<Integer> newNodes = new HashSet<>(originalNodes);
        List<Pair<Integer, Integer>> newEdges = new ArrayList<>(originalEdges);
        Collections.shuffle(newEdges, new Random(System.nanoTime()));
        return Pair.of(newNodes, newEdges);
    }

    private static String nodeShiftingSer(List<Integer> original2New) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("original_node_id").append("->").append("shifted_node_id").append("\n");
        for (int i = 0; i < original2New.size(); i++) {
            stringBuilder.append(i).append("->").append(original2New.get(i)).append("\n");
        }
        return stringBuilder.toString();
    }
}
