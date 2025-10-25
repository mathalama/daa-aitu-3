package metrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.Locale;

public final class MetricsCsv {
    private static final String HEADER =
            "id,algorithm,vertices,total_cost,time_ms,operations\n";
    private static final DecimalFormat DF =
            (DecimalFormat) DecimalFormat.getInstance(Locale.US);

    static { DF.applyPattern("0.########"); }

    private MetricsCsv() {}

    public static synchronized void writeRow(
            Path csvPath,
            String graphId,
            String algorithm,
            int vertices,
            double totalCost,
            double timeMs,
            long operations
    ) {
        try {
            boolean exists = Files.exists(csvPath);
            if (!exists) {
                if (csvPath.getParent() != null)
                    Files.createDirectories(csvPath.getParent());
                Files.writeString(csvPath, HEADER, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }

            String line = String.format(Locale.US,
                    "%s,%s,%d,%.8f,%.3f,%d%n",
                    escape(graphId), escape(algorithm),
                    vertices, totalCost, timeMs, operations);

            Files.writeString(csvPath, line, StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND);

        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV: " + csvPath, e);
        }
    }

    private static String escape(String s) {
        return (s.contains(",") || s.contains("\"") || s.contains("\n"))
                ? "\"" + s.replace("\"", "\"\"") + "\""
                : s;
    }
}
