package com.resilientcache.controller;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.core.CacheManager;
import com.resilientcache.event.HealthStatus;
import com.resilientcache.monitoring.EventTracker;
import com.resilientcache.monitoring.HealthScheduler;
import com.resilientcache.monitoring.MetricsTracker;
import com.resilientcache.provider.caffeine.CaffeineAdapter;
import com.resilientcache.provider.redis.RedisAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheMonitorControllerTest {

    private CaffeineAdapter caffeineAdapter;
    private RedisAdapter redisAdapter;
    private MetricsTracker metricsTracker;
    private EventTracker eventTracker;
    private CacheManager cacheManager;
    private CacheProperties properties;
    private HealthScheduler healthScheduler;
    private com.resilientcache.monitoring.AnalyticsEngine analyticsEngine;
    private CacheMonitorController controller;

    @BeforeEach
    void setUp() {
        caffeineAdapter = mock(CaffeineAdapter.class);
        redisAdapter = mock(RedisAdapter.class);
        metricsTracker = mock(MetricsTracker.class);
        eventTracker = mock(EventTracker.class);
        cacheManager = mock(CacheManager.class);
        properties = new CacheProperties();
        properties.setProvider("multi-level");
        healthScheduler = mock(HealthScheduler.class);
        analyticsEngine = mock(com.resilientcache.monitoring.AnalyticsEngine.class);
        
        controller = new CacheMonitorController(
            caffeineAdapter,
            redisAdapter,
            metricsTracker,
            eventTracker,
            cacheManager,
            properties,
            healthScheduler,
            analyticsEngine
        );
    }

    @Test
    void testGetStats() {
        com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats = mock(com.github.benmanes.caffeine.cache.stats.CacheStats.class);
        when(caffeineStats.hitCount()).thenReturn(10L);
        when(caffeineStats.missCount()).thenReturn(5L);
        when(caffeineStats.hitRate()).thenReturn(0.66);
        when(caffeineAdapter.getStats()).thenReturn(caffeineStats);

        when(metricsTracker.getL2HitCount()).thenReturn(20L);
        when(metricsTracker.getL2MissCount()).thenReturn(10L);
        when(metricsTracker.getAverageReadLatencyMs()).thenReturn(1.5);
        when(metricsTracker.getAverageWriteLatencyMs()).thenReturn(3.2);

        ResponseEntity<Map<String, Object>> response = controller.getStats();
        Map<String, Object> body = response.getBody();

        assertNotNull(body);
        assertEquals("multi-level", body.get("provider"));
        assertEquals(10L, body.get("l1Hits"));
        assertEquals(5L, body.get("l1Misses"));
        assertEquals(0.66, body.get("l1HitRatio"));
        assertEquals(20L, body.get("l2Hits"));
        assertEquals(10L, body.get("l2Misses"));
        assertEquals(20.0 / 30.0, body.get("l2HitRatio"));
        assertEquals(1.5, body.get("averageReadLatencyMs"));
        assertEquals(3.2, body.get("averageWriteLatencyMs"));
    }

    @Test
    void testGetHealth() {
        when(healthScheduler.getCurrentStatus()).thenReturn(HealthStatus.UP);
        when(redisAdapter.healthCheck()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.getHealth();
        Map<String, Object> body = response.getBody();

        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertEquals("UP", body.get("l1Status"));
        assertEquals("UP", body.get("l2Status"));
    }

    @Test
    void testGetEvents() {
        List<Map<String, Object>> mockEvents = new ArrayList<>();
        Map<String, Object> mockEvent = new HashMap<>();
        mockEvent.put("type", "FAILURE");
        mockEvents.add(mockEvent);
        when(eventTracker.getEvents()).thenReturn(mockEvents);

        ResponseEntity<List<Map<String, Object>>> response = controller.getEvents();
        List<Map<String, Object>> body = response.getBody();

        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("FAILURE", body.get(0).get("type"));
    }

    @Test
    void testClearCache() {
        ResponseEntity<Map<String, String>> response = controller.clearCache();
        Map<String, String> body = response.getBody();

        assertNotNull(body);
        assertEquals("Cache cleared successfully", body.get("message"));
        verify(cacheManager, times(1)).clear();
        verify(eventTracker, times(1)).addManualEvent(eq("CACHE_CLEAR"), eq("multi-level"), anyString());
    }
}
