package util;

import model.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class DotOut {
    private DotOut(){}

    public static void write(Path dotPath, Graph g, List<Edge> mstEdges) {
        try {
            if (dotPath.getParent()!=null) Files.createDirectories(dotPath.getParent());
            Set<Long> mst = new HashSet<>();
            if (mstEdges != null)
                for (Edge e : mstEdges) mst.add(key(e.u(), e.v()));

            StringBuilder sb = new StringBuilder(1024);
            sb.append("graph G {\n  layout=dot;\n  overlap=false;\n  splines=true;\n  rankdir=LR;\n");
            for (int i=0;i<g.V();i++) sb.append("  ").append(i).append(" [shape=circle];\n");
            for (Edge e : g.edges()) {
                boolean inMst = mst.contains(key(e.u(), e.v())) || mst.contains(key(e.v(), e.u()));
                sb.append("  ").append(e.u()).append(" -- ").append(e.v())
                        .append(" [label=\"").append(String.format(Locale.US,"%.2f",e.w())).append("\"");
                sb.append(inMst ? ", penwidth=3, color=blue" : ", color=gray, style=dashed");
                sb.append("];\n");
            }
            sb.append("}\n");
            Files.writeString(dotPath, sb.toString());
        } catch (IOException e) { throw new RuntimeException("DOT write failed: " + dotPath, e); }
    }

    private static long key(int a,int b){ int x=Math.min(a,b), y=Math.max(a,b); return (((long)x)<<32) ^ (y&0xffffffffL); }
}
