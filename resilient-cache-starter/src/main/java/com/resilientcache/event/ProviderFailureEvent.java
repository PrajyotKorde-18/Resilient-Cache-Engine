package com.resilientcache.event;

public class ProviderFailureEvent {

    private final String providerName;
    private final String reason;
    private final long timestamp;

    public ProviderFailureEvent(String providerName, String reason) {
        this.providerName = providerName;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
    }

    public String getProviderName() {
        return providerName;
    }

    public String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
