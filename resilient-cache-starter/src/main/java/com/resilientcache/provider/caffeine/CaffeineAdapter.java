package com.resilientcache.provider.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.resilientcache.config.CacheProperties;
import com.resilientcache.port.CachePort;

import java.time.Duration;
import java.util.Optional;

public class CaffeineAdapter implements CachePort {

    private final Cache<String, CacheValue> cache;
    private final CacheProperties properties;

    public CaffeineAdapter(CacheProperties properties) {
        this.properties = properties;
        this.cache = Caffeine.newBuilder()
                .maximumSize(properties.getCaffeine().getMaximumSize())
                .recordStats()
                .expireAfter(new Expiry<String, CacheValue>() {
                    @Override
                    public long expireAfterCreate(String key, CacheValue value, long currentTime) {
                        return value.getTtl().toNanos();
                    }

                    @Override
                    public long expireAfterUpdate(String key, CacheValue value, long currentTime, long currentDuration) {
                        return value.getTtl().toNanos();
                    }

                    @Override
                    public long expireAfterRead(String key, CacheValue value, long currentTime, long currentDuration) {
                        return currentDuration; // Keep current remaining TTL on read
                    }
                })
                .build();
    }

    public com.github.benmanes.caffeine.cache.stats.CacheStats getStats() {
        return cache.stats();
    }


    @Override
    public Optional<Object> get(String key) {
        CacheValue cacheValue = cache.getIfPresent(key);
        if (cacheValue == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cacheValue.getValue());
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        if (value == null) {
            return;
        }
        Duration effectiveTtl = ttl != null ? ttl : properties.getCaffeine().getExpireAfterWrite();
        cache.put(key, new CacheValue(value, effectiveTtl));
    }

    @Override
    public void evict(String key) {
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    public boolean healthCheck() {
        // Local in-memory cache is always healthy unless JVM itself is out of memory
        return true;
    }

    @Override
    public long getSize() {
        return cache.estimatedSize();
    }

    /**
     * Wrapper class to store cache value along with its specific TTL duration.
     */
    private static class CacheValue {
        private final Object value;
        private final Duration ttl;

        public CacheValue(Object value, Duration ttl) {
            this.value = value;
            this.ttl = ttl;
        }

        public Object getValue() {
            return value;
        }

        public Duration getTtl() {
            return ttl;
        }
    }
}
