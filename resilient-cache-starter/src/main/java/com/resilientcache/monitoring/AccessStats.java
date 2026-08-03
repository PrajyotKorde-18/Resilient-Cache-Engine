package com.resilientcache.monitoring;

public class AccessStats {

    private final String key;
    private long accessCount;
    private long lastAccessTime;

    public AccessStats(String key) {
        this.key = key;
        this.accessCount = 0;
        this.lastAccessTime = System.currentTimeMillis();
    }

    public synchronized void recordAccess() {
        this.accessCount++;
        this.lastAccessTime = System.currentTimeMillis();
    }

    public String getKey() {
        return key;
    }

    public synchronized long getAccessCount() {
        return accessCount;
    }

    public synchronized long getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     * Calculates a time-decayed popularity score.
     * Score = accessCount / (1 + minutesPassedSinceLastAccess)
     * This ensures recently accessed items score significantly higher than old traffic.
     */
    public synchronized double calculateHotScore() {
        double minutesSinceAccess = (System.currentTimeMillis() - lastAccessTime) / 60000.0;
        return accessCount / (1.0 + minutesSinceAccess);
    }
}
