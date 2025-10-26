package io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import model.Edge;
import model.Graph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class JsonReader {
    private static final ObjectMapper M = new ObjectMapper();
    private JsonReader() {}

    public static List<Graph> readGraphs(Path path) {
        try {
            JsonNode root = M.readTree(Files.readAllBytes(path));
            ArrayNode arr = root.isArray() ? (ArrayNode) root
                    : (ArrayNode) root.path("graphs");
            if (arr == null || !arr.isArray()) {
                throw new IllegalArgumentException("expected array at root[] or $.graphs");
            }

            List<Graph> out = new ArrayList<>();

            for (JsonNode g : arr) {
                String id = g.path("id").isMissingNode()
                        ? "graph-" + out.size()
                        : g.get("id").asText();

                JsonNode edgesNode = g.path("edges");
                if (!edgesNode.isArray()) {
                    throw new IllegalArgumentException("graph " + id + ": edges[] required");
                }

                List<String> labels = new ArrayList<>();
                JsonNode nodesNode = g.path("nodes");
                if (nodesNode.isArray()) {
                    for (JsonNode nn : nodesNode) labels.add(nn.asText());
                }

                List<int[]> numericEdges = new ArrayList<>();
                List<String[]> labeledEdges = new ArrayList<>();
                List<Double> edgeWeights = new ArrayList<>();

                for (JsonNode e : edgesNode) {
                    JsonNode nu = e.get("u"), nv = e.get("v");
                    JsonNode nf = e.get("from"), nt = e.get("to");

                    double w;
                    if (!e.path("w").isMissingNode())       w = e.get("w").asDouble();
                    else if (!e.path("weight").isMissingNode()) w = e.get("weight").asDouble();
                    else w = 0.0;

                    if (nu != null && nv != null) {
                        numericEdges.add(new int[]{nu.asInt(), nv.asInt()});
                        edgeWeights.add(w);
                    } else if (nf != null && nt != null) {
                        labeledEdges.add(new String[]{nf.asText(), nt.asText()});
                        edgeWeights.add(w);
                    } else {
                        throw new IllegalArgumentException("graph " + id + ": bad edge format");
                    }
                }

                List<Edge> edges = new ArrayList<>();

                if (!labeledEdges.isEmpty() && numericEdges.isEmpty()) {
                    if (labels.isEmpty()) {
                        LinkedHashSet<String> set = new LinkedHashSet<>();
                        for (String[] p : labeledEdges) { set.add(p[0]); set.add(p[1]); }
                        labels = new ArrayList<>(set);
                    }
                    Map<String,Integer> idx = new HashMap<>();
                    for (int i = 0; i < labels.size(); i++) idx.put(labels.get(i), i);
                    for (String[] p : labeledEdges) {
                        if (!idx.containsKey(p[0])) { idx.put(p[0], labels.size()); labels.add(p[0]); }
                        if (!idx.containsKey(p[1])) { idx.put(p[1], labels.size()); labels.add(p[1]); }
                    }
                    for (int i = 0; i < labeledEdges.size(); i++) {
                        String[] p = labeledEdges.get(i);
                        int u = idx.get(p[0]), v = idx.get(p[1]);
                        edges.add(new Edge(u, v, edgeWeights.get(i)));
                    }
                } else {
                    int max = -1;
                    for (int[] uv : numericEdges) max = Math.max(max, Math.max(uv[0], uv[1]));
                    int needV = max + 1;

                    int Vjson = g.path("vertices").asInt(-1);
                    if (Vjson > 0) needV = Math.max(needV, Vjson);

                    if (labels.isEmpty()) {
                        for (int i = 0; i < needV; i++) labels.add(String.valueOf(i));
                    } else if (labels.size() < needV) {
                        for (int i = labels.size(); i < needV; i++) labels.add(String.valueOf(i));
                    }
                    for (int i = 0; i < numericEdges.size(); i++) {
                        int[] uv = numericEdges.get(i);
                        edges.add(new Edge(uv[0], uv[1], edgeWeights.get(i)));
                    }
                }

                if (labels.isEmpty()) {
                    throw new IllegalArgumentException("graph " + id + ": no nodes and no edges");
                }

                int Vjson = g.path("vertices").asInt(-1);
                if (Vjson > 0 && Vjson > labels.size()) {
                    for (int i = labels.size(); i < Vjson; i++) labels.add(String.valueOf(i));
                }

                out.add(new Graph(id, labels, edges));
            }

            return out;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read input: " + path, ex);
        }
    }
}
