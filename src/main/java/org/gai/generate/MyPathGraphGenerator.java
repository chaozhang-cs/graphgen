package org.gai.generate;

import org.jgrapht.Graph;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class MyPathGraphGenerator implements GraphGenerator<Integer, DefaultEdge, Integer> {

    private final int n;
    private final Random random;

    public MyPathGraphGenerator(int n, Random random) {
        if (n < 2) {
            throw new IllegalArgumentException("Number of vertices must be greater than 2");
        } else {
            this.n = n;
        }
        if (random == null) {
            throw new IllegalArgumentException("Random must not be null");
        } else {
            this.random = random;
        }
    }

    @Override
    public void generateGraph(Graph<Integer, DefaultEdge> graph, Map<String, Integer> map) {
        List<Integer> integers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph.addVertex(i);
            integers.add(i);
        }
        Collections.shuffle(integers, random);
        for (int i = 0; i < integers.size() - 1; i++)
            graph.addEdge(integers.get(i), integers.get(i + 1));

    }
}
