package com.resilientcache.port;

public interface CacheWarmupLoader {

    /**
     * Determines if this loader handles a specific cache namespace (e.g. "customers").
     */
    boolean supports(String cacheName);

    /**
     * Loads the fresh value from the database for the given key.
     */
    Object load(String key);
}
