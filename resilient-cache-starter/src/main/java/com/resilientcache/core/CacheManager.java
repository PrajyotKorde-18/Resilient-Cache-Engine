package com.resilientcache.core;

import com.resilientcache.port.CachePort;

import java.time.Duration;
import java.util.Optional;

public class CacheManager {

    private final CachePort activeCachePort;

    public CacheManager(CachePort activeCachePort) {
        this.activeCachePort = activeCachePort;
    }

    public Optional<Object> get(String key) {
        return activeCachePort.get(key);
    }

    public void put(String key, Object value, Duration ttl) {
        activeCachePort.put(key, value, ttl);
    }

    public void evict(String key) {
        activeCachePort.evict(key);
    }

    public void clear() {
        activeCachePort.clear();
    }
}
