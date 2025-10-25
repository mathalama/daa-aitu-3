package metrics;

public final class Stopwatch implements AutoCloseable {
    private final long start = System.nanoTime();
    private long elapsed = -1;

    public double elapsedMillis() {
        return (elapsed >= 0 ? elapsed : System.nanoTime() - start) / 1_000_000.0;
    }

    @Override
    public void close() {
        if (elapsed < 0) elapsed = System.nanoTime() - start;
    }
}
