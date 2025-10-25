package metrics;

public final class OpCounter {
    private long c;
    public void inc() { c++; }
    public void add(long n) { c += n; }
    public long get() { return c; }
    public void reset() { c = 0; }
}
