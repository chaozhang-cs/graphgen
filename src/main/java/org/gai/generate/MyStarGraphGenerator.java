package org.gai.generate;

import org.jgrapht.Graph;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultEdge;

import java.util.Map;

public class MyStarGraphGenerator implements GraphGenerator<Integer, DefaultEdge, Integer> {
    private final int centerVertex;
    private final int n;

    public MyStarGraphGenerator(int n, int centerVertex) {
        if (n < 4) {
            throw new IllegalArgumentException("Number of vertices must be greater than 4");
        } else {
            this.n = n;
        }
        if (centerVertex < 0) {
            throw new IllegalArgumentException("centerVertex must be non-negative");
        } else {
            this.centerVertex = centerVertex;
        }
    }

    @Override
    public void generateGraph(Graph<Integer, DefaultEdge> graph, Map<String, Integer> map) {
        graph.addVertex(centerVertex);
        if (map != null)
            map.put("Center Vertex", centerVertex);

        for (int i = 0; i < n; i++) {
            graph.addVertex(i);
            if (i == centerVertex)
                continue;
            graph.addEdge(centerVertex, i);
        }
    }

}
