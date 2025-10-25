package mst;

import java.util.List;
import model.Edge;

public record MstCoreResult(List<Edge> treeEdges, double totalCost) {}

