package com.resilientcache.monitoring;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.port.HotKeyPort;
import com.resilientcache.warmup.HotKeyRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public class HotKeyScheduler {

    private static final Logger log = LoggerFactory.getLogger(HotKeyScheduler.class);

    private final AnalyticsEngine analyticsEngine;
    private final ObjectProvider<HotKeyPort> hotKeyPortProvider;
    private final CacheProperties properties;

    public HotKeyScheduler(AnalyticsEngine analyticsEngine,
                             ObjectProvider<HotKeyPort> hotKeyPortProvider,
                             CacheProperties properties) {
        this.analyticsEngine = analyticsEngine;
        this.hotKeyPortProvider = hotKeyPortProvider;
        this.properties = properties;
    }

    /**
     * Periodically extracts and persists top-scored hot keys.
     * Default scheduler rate is every 60 seconds.
     */
    @Scheduled(fixedRateString = "${resilient-cache.warmup.scheduler-rate-ms:60000}")
    public void persistTopHotKeys() {
        if (!properties.getWarmup().isEnabled()) {
            return;
        }

        HotKeyPort hotKeyPort = hotKeyPortProvider.getIfAvailable();
        if (hotKeyPort == null) {
            log.debug("Skipping hot key persistence: No HotKeyPort adapter is registered.");
            return;
        }

        try {
            int limit = properties.getWarmup().getTopNKeys();
            List<HotKeyRecord> topKeys = analyticsEngine.extractTopHotKeys(limit);
            if (!topKeys.isEmpty()) {
                hotKeyPort.saveTopHotKeys(topKeys);
                log.info("Persisted {} hot keys to SQL database", topKeys.size());
            }
        } catch (Exception e) {
            log.error("Failed to persist hot keys: {}", e.getMessage());
        }
    }
}
