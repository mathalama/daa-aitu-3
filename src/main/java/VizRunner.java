import algo.*;
import io.*;
import metrics.*;
import model.*;
import mst.*;
import util.*;

import java.nio.file.*;

public final class VizRunner {
    public static void main(String[] args) {
        Path input = Path.of(System.getProperty("input", "json/smallGraphs.json")).toAbsolutePath();
        Path outDir = Path.of("GraphImages").toAbsolutePath();

        var graphs = JsonReader.readGraphs(input);
        MstAlgorithm prim = new Prim(new OpCounter());

        try { Files.createDirectories(outDir); } catch (Exception e) { throw new RuntimeException(e); }

        for (Graph g : graphs) {
            var mst = prim.run(g).treeEdges();
            Path dot = outDir.resolve("graph-" + g.id() + ".dot");
            DotOut.write(dot, g, mst);
            System.out.println("Wrote: " + dot);
        }
    }
}
