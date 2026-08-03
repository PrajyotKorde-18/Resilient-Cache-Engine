package com.resilientcache.event;

public class RecoveryStartedEvent {

    private final String providerName;
    private final long timestamp;

    public RecoveryStartedEvent(String providerName) {
        this.providerName = providerName;
        this.timestamp = System.currentTimeMillis();
    }

    public String getProviderName() {
        return providerName;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
