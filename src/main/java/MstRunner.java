import algo.*;
import io.JsonReader;
import metrics.*;
import model.*;
import mst.*;
import util.*;

import java.nio.file.Path;
import java.util.*;

public final class MstRunner {
    public static void main(String[] args) {
        Path in   = Path.of(System.getProperty("input",   "json/smallGraphs.json"));
        Path jout = Path.of(System.getProperty("jsonOut", "json/output.json"));
        Path cout = Path.of(System.getProperty("csvOut",  "metrics/mst_metrics.csv"));

        List<Graph> graphs = JsonReader.readGraphs(in);
        Map<String, List<MstResult>> byGraph = new LinkedHashMap<>();

        for (Graph g : graphs) {
            List<MstResult> list = new ArrayList<>();

            OpCounter opsPrim = new OpCounter();
            MstAlgorithm prim = new Prim(opsPrim);
            MstResult rPrim = AlgorithmRecorder.runWithMetrics(
                    "Prim", prim, g,
                    opsPrim, new Stopwatch(),
                    (path, r) -> MetricsCsv.writeRow(path, r.graphId(), r.algorithm(), r.vertices(),
                            r.totalCost(), r.timeMs(), r.operations()),
                    cout
            );
            list.add(rPrim);

            OpCounter opsK = new OpCounter();
            MstAlgorithm kruskal = new Kruskal(opsK);
            MstResult rK = AlgorithmRecorder.runWithMetrics(
                    "Kruskal", kruskal, g,
                    opsK, new Stopwatch(),
                    (path, r) -> MetricsCsv.writeRow(path, r.graphId(), r.algorithm(), r.vertices(),
                            r.totalCost(), r.timeMs(), r.operations()),
                    cout
            );
            list.add(rK);

            byGraph.put(g.id(), list);
        }

        JsonOut.write(jout, byGraph);
    }
}
