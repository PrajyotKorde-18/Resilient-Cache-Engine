package com.resilientcache.core;

import com.resilientcache.config.CacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public class CacheEngine {

    private static final Logger log = LoggerFactory.getLogger(CacheEngine.class);

    private final CacheManager cacheManager;
    private final CacheProperties properties;
    private final com.resilientcache.monitoring.MetricsTracker metricsTracker;

    public CacheEngine(CacheManager cacheManager, CacheProperties properties, com.resilientcache.monitoring.MetricsTracker metricsTracker) {
        this.cacheManager = cacheManager;
        this.properties = properties;
        this.metricsTracker = metricsTracker;
    }

    /**
     * Retrieves a value from the cache or loads it from the origin if missing (Cache-Aside pattern).
     *
     * @param key         the cache key
     * @param valueLoader supplier to compute the value on a cache miss
     * @param ttlOverride optional TTL override
     * @return the cached or computed value
     */
    public Object get(String key, Supplier<Object> valueLoader, Duration ttlOverride) {
        long start = System.nanoTime();
        
        Optional<Object> cached = cacheManager.get(key);
        long latencyNs = System.nanoTime() - start;
        long latency = latencyNs / 1_000_000;

        if (metricsTracker != null) {
            metricsTracker.recordReadLatency(latencyNs);
        }

        // Capitalize for matching formatting (e.g. CAFFEINE, REDIS)
        String providerName = properties.getProvider().substring(0, 1).toUpperCase() 
                + properties.getProvider().substring(1).toLowerCase();

        if (cached.isPresent()) {
            log.info("Cache HIT  | Provider={} | Key={} | Latency={}ms", providerName, key, latency);
            return cached.get();
        }

        log.info("Cache MISS | Provider={} | Key={}", providerName, key);
        
        // Cache miss: load from database / origin method
        log.info("Loading from Database | Key={}", key);
        Object computed = valueLoader.get();
        
        if (computed != null) {
            Duration effectiveTtl = ttlOverride != null ? ttlOverride : properties.getDefaultTtl();
            
            long putStart = System.nanoTime();
            cacheManager.put(key, computed, effectiveTtl);
            long putLatencyNs = System.nanoTime() - putStart;
            
            if (metricsTracker != null) {
                metricsTracker.recordWriteLatency(putLatencyNs);
            }
            
            log.info("Stored in {} | TTL={}", providerName, formatDuration(effectiveTtl));
        }
        
        return computed;
    }


    public void evict(String key) {
        cacheManager.evict(key);
        log.info("Evicted key: {}", key);
    }

    public void clear() {
        cacheManager.clear();
        log.info("Cleared cache");
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "null";
        }
        long minutes = duration.toMinutes();
        if (minutes > 0) {
            return minutes + "m";
        }
        return duration.toSeconds() + "s";
    }
}
