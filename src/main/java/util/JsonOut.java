package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mst.MstResult;
import model.Edge;

import java.nio.file.*;
import java.util.*;
import java.io.IOException;

public final class JsonOut {
    private static final ObjectMapper M = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonOut() {}

    public static void write(Path out, Map<String, List<MstResult>> byGraph) {
        List<Map<String,Object>> flat = new ArrayList<>();
        for (var entry : byGraph.entrySet()) {
            for (MstResult r : entry.getValue()) {
                Map<String,Object> o = new LinkedHashMap<>();
                o.put("graphId", r.graphId());
                o.put("algorithm", r.algorithm());
                o.put("vertices", r.vertices());
                o.put("edgesInMst", r.treeEdges().size());
                o.put("totalCost", r.totalCost());
                o.put("timeMs", r.timeMs());
                o.put("operations", r.operations());
                o.put("acyclic", r.isAcyclic());
                o.put("connected", r.isConnected());

                List<int[]> edges = new ArrayList<>(r.treeEdges().size());
                for (Edge e : r.treeEdges()) edges.add(new int[]{e.u(), e.v()});
                o.put("mstEdges", edges);

                flat.add(o);
            }
        }
        try {
            if (out.getParent()!=null) Files.createDirectories(out.getParent());
            M.writeValue(out.toFile(), flat);
        } catch (IOException e) {
            throw new RuntimeException("JSON write failed: " + out, e);
        }
    }
}
