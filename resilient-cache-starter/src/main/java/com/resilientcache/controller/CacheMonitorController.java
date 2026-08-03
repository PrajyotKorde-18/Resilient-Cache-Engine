package com.resilientcache.controller;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.core.CacheManager;
import com.resilientcache.event.HealthStatus;
import com.resilientcache.monitoring.EventTracker;
import com.resilientcache.monitoring.HealthScheduler;
import com.resilientcache.monitoring.MetricsTracker;
import com.resilientcache.provider.caffeine.CaffeineAdapter;
import com.resilientcache.provider.redis.RedisAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheMonitorController {

    private final CaffeineAdapter caffeineAdapter;
    private final RedisAdapter redisAdapter;
    private final MetricsTracker metricsTracker;
    private final EventTracker eventTracker;
    private final CacheManager cacheManager;
    private final CacheProperties properties;
    private final HealthScheduler healthScheduler;
    private final com.resilientcache.monitoring.AnalyticsEngine analyticsEngine;

    public CacheMonitorController(
            CaffeineAdapter caffeineAdapter,
            @Autowired(required = false) RedisAdapter redisAdapter,
            MetricsTracker metricsTracker,
            EventTracker eventTracker,
            CacheManager cacheManager,
            CacheProperties properties,
            HealthScheduler healthScheduler,
            com.resilientcache.monitoring.AnalyticsEngine analyticsEngine) {
        this.caffeineAdapter = caffeineAdapter;
        this.redisAdapter = redisAdapter;
        this.metricsTracker = metricsTracker;
        this.eventTracker = eventTracker;
        this.cacheManager = cacheManager;
        this.properties = properties;
        this.healthScheduler = healthScheduler;
        this.analyticsEngine = analyticsEngine;
    }

    @GetMapping("/hotkeys")
    public ResponseEntity<List<com.resilientcache.warmup.HotKeyRecord>> getHotKeys() {
        int limit = properties.getWarmup().getTopNKeys();
        return ResponseEntity.ok(analyticsEngine.extractTopHotKeys(limit));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // L1 Caffeine Stats & Size
        com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats = caffeineAdapter.getStats();
        long l1Hits = caffeineStats.hitCount();
        long l1Misses = caffeineStats.missCount();
        double l1HitRatio = caffeineStats.hitRate();
        long l1Size = caffeineAdapter.getSize();

        // L2 Redis Stats & Size
        long l2Hits = metricsTracker.getL2HitCount();
        long l2Misses = metricsTracker.getL2MissCount();
        long totalL2Queries = l2Hits + l2Misses;
        double l2HitRatio = totalL2Queries == 0 ? 0.0 : (double) l2Hits / totalL2Queries;
        long l2Size = redisAdapter != null ? redisAdapter.getSize() : 0;

        stats.put("provider", properties.getProvider());
        stats.put("l1Hits", l1Hits);
        stats.put("l1Misses", l1Misses);
        stats.put("l1HitRatio", l1HitRatio);
        stats.put("l1Size", l1Size);
        
        stats.put("l2Hits", l2Hits);
        stats.put("l2Misses", l2Misses);
        stats.put("l2HitRatio", l2HitRatio);
        stats.put("l2Size", l2Size);

        stats.put("averageReadLatencyMs", metricsTracker.getAverageReadLatencyMs());
        stats.put("averageWriteLatencyMs", metricsTracker.getAverageWriteLatencyMs());
        
        // JVM Statistics
        Runtime runtime = Runtime.getRuntime();
        stats.put("jvmMaxMemory", runtime.maxMemory());
        stats.put("jvmTotalMemory", runtime.totalMemory());
        stats.put("jvmFreeMemory", runtime.freeMemory());
        stats.put("jvmUsedMemory", runtime.totalMemory() - runtime.freeMemory());
        stats.put("jvmThreads", Thread.activeCount());
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        
        HealthStatus engineStatus = healthScheduler.getCurrentStatus();
        health.put("status", engineStatus.name());
        health.put("l1Status", "UP"); // Caffeine is local in-memory, always UP
        
        boolean l2Healthy = false;
        if (redisAdapter != null) {
            try {
                l2Healthy = redisAdapter.healthCheck();
            } catch (Exception e) {
                // Ignore and keep false
            }
        }
        health.put("l2Status", l2Healthy ? "UP" : "DOWN");
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/events")
    public ResponseEntity<List<Map<String, Object>>> getEvents() {
        return ResponseEntity.ok(eventTracker.getEvents());
    }

    @PostMapping("/evict")
    public ResponseEntity<Map<String, String>> evictKey(@org.springframework.web.bind.annotation.RequestParam String key) {
        cacheManager.evict(key);
        
        eventTracker.addManualEvent(
            "CACHE_EVICT", 
            properties.getProvider(), 
            "Evicted cache key: " + key
        );
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Key '" + key + "' evicted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCache() {
        cacheManager.clear();
        
        eventTracker.addManualEvent(
            "CACHE_CLEAR", 
            properties.getProvider(), 
            "Cache cleared manually by admin command"
        );
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cache cleared successfully");
        return ResponseEntity.ok(response);
    }
}
