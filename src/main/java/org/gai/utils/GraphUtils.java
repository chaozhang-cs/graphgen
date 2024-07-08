package org.gai.utils;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;


import java.util.ArrayList;
import java.util.List;

public class GraphUtils {
    public static List<Pair<Integer, Integer>> getEdgeList(Graph<Integer, DefaultEdge> g) {
        List<Pair<Integer, Integer>> edgeList = new ArrayList<>();
        for (DefaultEdge e : g.edgeSet())
            edgeList.add(Pair.of(g.getEdgeSource(e), g.getEdgeTarget(e)));
        return edgeList;
    }


}
