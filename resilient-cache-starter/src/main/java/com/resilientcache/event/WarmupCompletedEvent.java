package com.resilientcache.event;

import org.springframework.context.ApplicationEvent;

public class WarmupCompletedEvent extends ApplicationEvent {

    private final String providerName;
    private final int keysLoaded;
    private final long durationMs;

    public WarmupCompletedEvent(Object source, String providerName, int keysLoaded, long durationMs) {
        super(source);
        this.providerName = providerName;
        this.keysLoaded = keysLoaded;
        this.durationMs = durationMs;
    }

    public String getProviderName() {
        return providerName;
    }

    public int getKeysLoaded() {
        return keysLoaded;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
