package com.resilientcache.event;

public class HealthStatusChangedEvent {

    private final String providerName;
    private final HealthStatus previousStatus;
    private final HealthStatus newStatus;
    private final long timestamp;

    public HealthStatusChangedEvent(String providerName, HealthStatus previousStatus, HealthStatus newStatus) {
        this.providerName = providerName;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.timestamp = System.currentTimeMillis();
    }

    public String getProviderName() {
        return providerName;
    }

    public HealthStatus getPreviousStatus() {
        return previousStatus;
    }

    public HealthStatus getNewStatus() {
        return newStatus;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
