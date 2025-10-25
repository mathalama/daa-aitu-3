package model;

import java.util.*;

public final class Graph {
    private final String id;
    private final List<String> labels;
    private final Map<String,Integer> idx;
    private final List<Edge> edges;

    public Graph(String id, List<String> nodeLabels, List<Edge> edges) {
        if (nodeLabels == null || nodeLabels.isEmpty()) throw new IllegalArgumentException("nodes empty");
        this.id = id;
        this.labels = List.copyOf(nodeLabels);
        this.idx = new HashMap<>();
        for (int i = 0; i < labels.size(); i++) {
            String name = labels.get(i);
            if (idx.putIfAbsent(name, i) != null) throw new IllegalArgumentException("dup node: " + name);
        }
        for (Edge e : edges) {
            if (e.u() >= labels.size() || e.v() >= labels.size())
                throw new IllegalArgumentException("vertex out of range");
        }
        this.edges = List.copyOf(edges);
    }

    public String id() { return id; }
    public int V() { return labels.size(); }
    public List<Edge> edges() { return edges; }

    public OptionalInt indexOf(String label) {
        Integer i = idx.get(label);
        return (i == null) ? OptionalInt.empty() : OptionalInt.of(i);
    }
    public String labelOf(int i) { return labels.get(i); }

    public static Graph fromLabeled(String id, List<String> nodeLabels, List<Triplet> labeledEdges) {
        Map<String,Integer> map = new HashMap<>();
        for (int i=0;i<nodeLabels.size();i++) map.put(nodeLabels.get(i), i);
        List<Edge> es = new ArrayList<>();
        for (Triplet t : labeledEdges) {
            Integer u = map.get(t.from), v = map.get(t.to);
            if (u == null || v == null) throw new IllegalArgumentException("unknown node in edge");
            es.add(new Edge(u, v, t.weight));
        }
        return new Graph(id, nodeLabels, es);
    }

    public static final class Triplet {
        public final String from, to; public final double weight;
        public Triplet(String from, String to, double weight) { this.from=from; this.to=to; this.weight=weight; }
    }
}
