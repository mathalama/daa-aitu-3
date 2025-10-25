import algo.Kruskal;
import algo.Prim;
import io.JsonReader;
import metrics.AlgorithmRecorder;
import metrics.MetricsCsv;
import metrics.OpCounter;
import metrics.Stopwatch;
import model.Graph;
import mst.MstAlgorithm;
import mst.MstResult;
import org.junit.jupiter.api.BeforeAll;
import util.JsonOut;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RunnerTest {

    @BeforeAll
    static void clearMetricsFile() {
        try {
            boolean deleted = Files.deleteIfExists(Paths.get("metrics/mst_metrics.csv"));
            if (deleted) {
                System.out.println("Metrics file deleted successfully.");
            }
        } catch (IOException e) {
            System.err.println("Error while trying to delete metrics file: " + e.getMessage());
        }
    }
    @Test
    void runPrimAndKruskal_WriteCsvAndJson_AndValidate() {
        Path in   = Path.of(System.getProperty("input",   "json/smallGraphs.json")).toAbsolutePath();
        Path jout = Path.of(System.getProperty("jsonOut", "json/output.json")).toAbsolutePath();
        Path cout = Path.of(System.getProperty("csvOut",  "metrics/mst_metrics.csv")).toAbsolutePath();

        var graphs = JsonReader.readGraphs(in);
        Map<String, List<MstResult>> byGraph = new LinkedHashMap<>();

        for (Graph g : graphs) {
            List<MstResult> perGraph = new ArrayList<>();

            OpCounter opsPrim = new OpCounter();
            MstAlgorithm prim = new Prim(opsPrim);
            MstResult rPrim = AlgorithmRecorder.runWithMetrics(
                    "Prim", prim, g, opsPrim, new Stopwatch(),
                    (path, r) -> MetricsCsv.writeRow(path, r.graphId(), r.algorithm(), r.vertices(),
                            r.totalCost(), r.timeMs(), r.operations()),
                    cout
            );
            perGraph.add(rPrim);

            OpCounter opsK = new OpCounter();
            MstAlgorithm kruskal = new Kruskal(opsK);
            MstResult rK = AlgorithmRecorder.runWithMetrics(
                    "Kruskal", kruskal, g, opsK, new Stopwatch(),
                    (path, r) -> MetricsCsv.writeRow(path, r.graphId(), r.algorithm(), r.vertices(),
                            r.totalCost(), r.timeMs(), r.operations()),
                    cout
            );
            perGraph.add(rK);

            assertTrue(rPrim.timeMs() >= 0 && rK.timeMs() >= 0);
            assertEquals(rPrim.totalCost(), rK.totalCost(), 1e-9, "Prim vs Kruskal total cost");
            if (rPrim.isConnected())  assertEquals(g.V() - 1, rPrim.treeEdges().size());
            if (rK.isConnected())     assertEquals(g.V() - 1, rK.treeEdges().size());
            assertTrue(rPrim.isAcyclic());
            assertTrue(rK.isAcyclic());

            byGraph.put(g.id(), perGraph);
        }

        JsonOut.write(jout, byGraph);
    }
}
