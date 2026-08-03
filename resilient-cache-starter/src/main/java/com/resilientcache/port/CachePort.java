package com.resilientcache.port;

import java.time.Duration;
import java.util.Optional;

public interface CachePort {

    /**
     * Retrieve value from cache.
     *
     * @param key cache key
     * @return Optional containing the cached value, or empty if miss/expired
     */
    Optional<Object> get(String key);

    /**
     * Write value to cache.
     *
     * @param key   cache key
     * @param value cache value
     * @param ttl   Time To Live duration
     */
    void put(String key, Object value, Duration ttl);

    /**
     * Remove value from cache.
     *
     * @param key cache key
     */
    void evict(String key);

    /**
     * Clear all cached values in this provider.
     */
    void clear();

    /**
     * Health status of this provider.
     *
     * @return true if UP and responsive, false otherwise
     */
    boolean healthCheck();

    /**
     * Estimated number of entries in this cache provider.
     *
     * @return cache size, or -1 if not supported
     */
    default long getSize() {
        return -1;
    }
}
