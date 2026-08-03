package com.resilientcache.warmup;

import java.io.Serializable;

public class HotKeyRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String key;
    private final double score;
    private final long lastAccessTime;

    public HotKeyRecord(String key, double score, long lastAccessTime) {
        this.key = key;
        this.score = score;
        this.lastAccessTime = lastAccessTime;
    }

    public String getKey() {
        return key;
    }

    public double getScore() {
        return score;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    @Override
    public String toString() {
        return "HotKeyRecord[Key=" + key + ", Score=" + String.format("%.2f", score) + "]";
    }
}
