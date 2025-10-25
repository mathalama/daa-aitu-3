package model;

public record Edge(int u, int v, double w) {
    public Edge {
        if (u < 0 || v < 0) throw new IllegalArgumentException("u,v >= 0");
        if (Double.isNaN(w) || Double.isInfinite(w)) throw new IllegalArgumentException("bad weight");
    }
}

