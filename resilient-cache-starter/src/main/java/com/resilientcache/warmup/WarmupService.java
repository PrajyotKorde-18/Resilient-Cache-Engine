package com.resilientcache.warmup;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.event.RecoveryCompletedEvent;
import com.resilientcache.event.WarmupCompletedEvent;
import com.resilientcache.port.CachePort;
import com.resilientcache.port.CacheWarmupLoader;
import com.resilientcache.port.HotKeyPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public class WarmupService {

    private static final Logger log = LoggerFactory.getLogger(WarmupService.class);

    private final CachePort activeCachePort;
    private final CacheProperties properties;
    private final ObjectProvider<HotKeyPort> hotKeyPortProvider;
    private final List<CacheWarmupLoader> warmupLoaders;
    private final ApplicationEventPublisher eventPublisher;

    public WarmupService(CachePort activeCachePort,
                         CacheProperties properties,
                         ObjectProvider<HotKeyPort> hotKeyPortProvider,
                         List<CacheWarmupLoader> warmupLoaders,
                         ApplicationEventPublisher eventPublisher) {
        this.activeCachePort = activeCachePort;
        this.properties = properties;
        this.hotKeyPortProvider = hotKeyPortProvider;
        this.warmupLoaders = warmupLoaders;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Listens asynchronously to recovery success and triggers preloading of hot keys.
     */
    @EventListener
    @Async("resilientCacheTaskExecutor")
    public void onRecoveryCompleted(RecoveryCompletedEvent event) {
        if (!properties.getWarmup().isEnabled()) {
            return;
        }

        log.info("Self-healing Warm-up started.");

        HotKeyPort hotKeyPort = hotKeyPortProvider.getIfAvailable();
        if (hotKeyPort == null) {
            log.warn("Warm-up skipped: No HotKeyPort adapter is registered.");
            return;
        }

        try {
            long startTime = System.currentTimeMillis();
            int limit = properties.getWarmup().getTopNKeys();
            List<HotKeyRecord> hotKeys = hotKeyPort.getTopHotKeys(limit);
            if (hotKeys.isEmpty()) {
                log.info("Warm-up finished: No hot keys found in database.");
                return;
            }

            log.info("Found {} hot keys to preload from database", hotKeys.size());
            int keysLoaded = 0;
            int batchSize = 20; // Throttled batch size to protect DB and Redis

            for (int i = 0; i < hotKeys.size(); i++) {
                HotKeyRecord record = hotKeys.get(i);
                preloadKey(record.getKey());
                keysLoaded++;

                // Throttled pause between batches
                if (keysLoaded % batchSize == 0 && (i + 1) < hotKeys.size()) {
                    log.info("Batch of {} keys preloaded. Pausing 100ms to protect database...", batchSize);
                    Thread.sleep(100);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Warm-up Completed successfully. Loaded {} keys.", keysLoaded);
            eventPublisher.publishEvent(new WarmupCompletedEvent(this, "Redis", keysLoaded, duration));

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Warm-up interrupted: {}", ie.getMessage());
        } catch (Exception e) {
            log.error("Warm-up failed: {}", e.getMessage());
        }
    }

    private void preloadKey(String key) {
        try {
            // Keys are formatted like "namespace:id" (e.g. "customers:15")
            int separatorIdx = key.indexOf(':');
            if (separatorIdx <= 0) {
                log.debug("Skipping key warm-up: invalid key format: {}", key);
                return;
            }

            String cacheName = key.substring(0, separatorIdx);
            CacheWarmupLoader matchingLoader = warmupLoaders.stream()
                    .filter(loader -> loader.supports(cacheName))
                    .findFirst()
                    .orElse(null);

            if (matchingLoader == null) {
                log.debug("No CacheWarmupLoader registered for namespace: {}", cacheName);
                return;
            }

            Object value = matchingLoader.load(key);
            if (value != null) {
                // Preload it back into L2 cache
                activeCachePort.put(key, value, properties.getDefaultTtl());
                log.debug("Preloaded hot key: {}", key);
            } else {
                log.debug("Skipping hot key (returned null from db): {}", key);
            }
        } catch (Exception e) {
            log.error("Failed to preload key {}: {}", key, e.getMessage());
        }
    }
}
