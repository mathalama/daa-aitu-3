package algo;

import metrics.OpCounter;
import model.*;
import mst.MstAlgorithm;
import mst.MstCoreResult;

import java.util.*;

public final class Prim implements MstAlgorithm {
    private final OpCounter ops;

    public Prim(OpCounter ops) { this.ops = ops; }

    @Override
    public MstCoreResult run(Graph g) {
        int V = g.V();
        if (V == 0) return new MstCoreResult(List.of(), 0.0);

        List<List<Edge>> adj = new ArrayList<>(V);
        for (int i=0;i<V;i++) adj.add(new ArrayList<>());
        for (Edge e : g.edges()) {
            adj.get(e.u()).add(e);
            adj.get(e.v()).add(new Edge(e.v(), e.u(), e.w()));
            ops.add(2);
        }

        boolean[] used = new boolean[V];
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingDouble(Edge::w));
        List<Edge> tree = new ArrayList<>();
        double cost = 0.0;

        for (int s=0; s<V; s++) if (!used[s]) {
            used[s] = true;
            pq.addAll(adj.get(s)); ops.add(adj.get(s).size());

            while (!pq.isEmpty()) {
                Edge e = pq.poll(); ops.inc();
                if (used[e.v()]) { ops.inc(); continue; }
                used[e.v()] = true;

                tree.add(new Edge(e.u(), e.v(), e.w())); ops.inc();
                cost += e.w();

                for (Edge nx : adj.get(e.v())) {
                    if (!used[nx.v()]) { pq.add(nx); ops.inc(); }
                }
            }
        }
        return new MstCoreResult(List.copyOf(tree), cost);
    }
}
