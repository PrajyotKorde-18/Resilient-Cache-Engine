package com.resilientcache.event;

public class RecoveryCompletedEvent {

    private final String providerName;
    private final int retryAttempts;
    private final long durationMs;
    private final long timestamp;

    public RecoveryCompletedEvent(String providerName, int retryAttempts, long durationMs) {
        this.providerName = providerName;
        this.retryAttempts = retryAttempts;
        this.durationMs = durationMs;
        this.timestamp = System.currentTimeMillis();
    }

    public String getProviderName() {
        return providerName;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
