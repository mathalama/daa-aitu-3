import algo.*;
import io.*;
import metrics.*;
import model.*;
import mst.*;
import util.*;

import java.nio.file.*;
import java.io.IOException;
import java.util.Comparator;

public final class VizRunner {
    public static void main(String[] args) {
        Path input = Path.of(System.getProperty("input", "json/smallGraphs.json")).toAbsolutePath();
        Path outDir = Path.of("graphDots").toAbsolutePath();

        clearDirectory(outDir);

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
    private static void clearDirectory(Path dir) {
        try {
            if (Files.exists(dir)) {
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .filter(p -> !p.equals(dir))
                        .forEach(p -> {
                            try { Files.delete(p); }
                            catch (IOException e) { System.err.println("Skip: " + p + " (" + e.getMessage() + ")"); }
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot clear GraphImages folder", e);
        }
    }
}
