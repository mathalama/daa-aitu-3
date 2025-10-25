package mst;

import java.util.List;
import model.Edge;

public final class GraphChecks {
    private GraphChecks() {}

    public static boolean isAcyclic(int V, List<Edge> edges) {
        UF uf = new UF(V);
        for (Edge e : edges) {
            int a = uf.find(e.u()), b = uf.find(e.v());
            if (a == b) return false;
            uf.union(a, b);
        }
        return true;
    }

    public static boolean isConnected(int V, List<Edge> edges) {
        if (V == 0) return true;
        UF uf = new UF(V);
        for (Edge e : edges) uf.union(e.u(), e.v());
        int r = uf.find(0);
        for (int i = 1; i < V; i++) if (uf.find(i) != r) return false;
        return true;
    }

    static final class UF {
        private final int[] p; private final byte[] r;
        UF(int n){ p=new int[n]; r=new byte[n]; for(int i=0;i<n;i++) p[i]=i; }
        int find(int x){ int a=x; while(a!=p[a]) a=p[a]; while(x!=a){int nx=p[x]; p[x]=a; x=nx;} return a; }
        void union(int a,int b){
            a=find(a); b=find(b); if(a==b) return;
            if(r[a]<r[b]) p[a]=b; else if(r[a]>r[b]) p[b]=a; else { p[b]=a; r[a]++; }
        }
    }
}
