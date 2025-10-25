package mst;

import java.util.List;
import model.Edge;

public record MstResult(
        String graphId,
        String algorithm,
        int vertices,
        List<Edge> treeEdges,
        double totalCost,
        double timeMs,
        long operations,
        boolean isAcyclic,
        boolean isConnected
) {}
