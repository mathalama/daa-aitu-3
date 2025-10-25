package io;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import model.Edge;
import model.Graph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class JsonReader {
    private static final ObjectMapper M = new ObjectMapper();
    private JsonReader() {}

    // Поддерживает:
    // 1) { "graphs": [ { "id":"g1","vertices":5,"edges":[{"u":0,"v":1,"w":2.5}, ...] }, ... ] }
    // 2) [ { "id":"g1","edges":[{"from":"A","to":"B","weight":1.2}, ...] }, ... ]
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
                String id = g.path("id").asText("graph-" + out.size());
                JsonNode edgesNode = g.path("edges");
                if (!edgesNode.isArray()) {
                    throw new IllegalArgumentException("graph " + id + ": edges[] required");
                }

                // накопим два вида рёбер
                List<int[]> numericEdges = new ArrayList<>();           // [u,v] для u/v/w
                List<String[]> labeledEdges = new ArrayList<>();        // [from,to] для from/to/weight
                List<Double> labeledWeights = new ArrayList<>();

                for (JsonNode e : edgesNode) {
                    JsonNode nu = e.get("u"), nv = e.get("v");
                    if (nu != null && nv != null) {
                        int u = nu.asInt();
                        int v = nv.asInt();
                        double w = e.path("w").isMissingNode() ? e.path("weight").asDouble()
                                : e.get("w").asDouble();
                        numericEdges.add(new int[]{u, v});
                        labeledWeights.add(w); // синхронный список весов
                    } else {
                        JsonNode nf = e.get("from"), nt = e.get("to");
                        if (nf == null || nt == null) {
                            throw new IllegalArgumentException("graph " + id + ": bad edge format");
                        }
                        double w = e.path("weight").isMissingNode() ? e.path("w").asDouble()
                                : e.get("weight").asDouble();
                        labeledEdges.add(new String[]{nf.asText(), nt.asText()});
                        labeledWeights.add(w);
                    }
                }

                List<Edge> edges = new ArrayList<>();
                List<String> labels;

                if (!labeledEdges.isEmpty() && numericEdges.isEmpty()) {
                    // формат from/to/weight: строим индексацию меток
                    LinkedHashSet<String> labelSet = new LinkedHashSet<>();
                    for (String[] p : labeledEdges) { labelSet.add(p[0]); labelSet.add(p[1]); }
                    labels = new ArrayList<>(labelSet);
                    Map<String,Integer> idx = new HashMap<>();
                    for (int i = 0; i < labels.size(); i++) idx.put(labels.get(i), i);

                    for (int i = 0; i < labeledEdges.size(); i++) {
                        String[] p = labeledEdges.get(i);
                        int u = idx.get(p[0]);
                        int v = idx.get(p[1]);
                        double w = labeledWeights.get(i);
                        edges.add(new Edge(u, v, w));
                    }
                } else {
                    // формат u/v/w: берём V = maxIndex+1 и генерим метки "0..V-1"
                    int max = -1;
                    for (int[] uv : numericEdges) {
                        max = Math.max(max, Math.max(uv[0], uv[1]));
                    }
                    int V = max + 1;
                    labels = new ArrayList<>(V);
                    for (int i = 0; i < V; i++) labels.add(String.valueOf(i));
                    for (int i = 0; i < numericEdges.size(); i++) {
                        int[] uv = numericEdges.get(i);
                        double w = labeledWeights.get(i); // веса уже синхронизированы
                        edges.add(new Edge(uv[0], uv[1], w));
                    }
                }

                // если указан vertices — переопределяем V и, при необходимости, дополняем labels
                int Vjson = g.path("vertices").asInt(-1);
                if (Vjson > 0 && Vjson > labels.size()) {
                    // дополним метки до нужного V
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
