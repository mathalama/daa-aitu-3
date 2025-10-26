package algo;

import metrics.OpCounter;
import model.*;
import mst.MstAlgorithm;
import mst.MstCoreResult;

import java.util.*;

public final class Kruskal implements MstAlgorithm {
    private final OpCounter ops;

    public Kruskal(OpCounter ops) { this.ops = ops; }

    private static final class UF {
        int[] p; byte[] r;
        UF(int n){ p=new int[n]; r=new byte[n]; for(int i=0;i<n;i++) p[i]=i; }
        int find(int x){
            int a=x;
            while(a!=p[a]) { a=p[a]; }
            while(x!=a){ int nx=p[x]; p[x]=a; x=nx; }
            return a;
        }
        boolean union(int a,int b){
            if((a=find(a))==(b=find(b))) return false;
            if(r[a]<r[b]) p[a]=b;
            else if(r[a]>r[b]) p[b]=a;
            else { p[b]=a; r[a]++; }
            return true;
        }
    }

    @Override
    public MstCoreResult run(Graph g) {
        List<Edge> sorted = new ArrayList<>(g.edges());
        ops.add(sorted.size());
        sorted.sort(Comparator.comparingDouble(Edge::w));

        UF uf = new UF(g.V());
        List<Edge> tree = new ArrayList<>(Math.max(0, g.V()-1));
        double cost = 0.0;

        for (Edge e : sorted) {
            ops.add(2);
            int u = uf.find(e.u());
            int v = uf.find(e.v());
            ops.inc();
            if (u != v && uf.union(u, v)) {
                ops.inc();
                tree.add(e);
                cost += e.w();
                if (tree.size() == Math.max(0, g.V()-1)) break;
            }
        }
        return new MstCoreResult(List.copyOf(tree), cost);
    }
}
