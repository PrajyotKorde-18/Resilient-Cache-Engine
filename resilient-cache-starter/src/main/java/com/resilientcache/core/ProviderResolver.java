package com.resilientcache.core;

import com.resilientcache.exception.ProviderNotConfiguredException;
import com.resilientcache.port.CachePort;
import com.resilientcache.provider.caffeine.CaffeineAdapter;
import com.resilientcache.provider.multi.MultiLevelCacheAdapter;
import com.resilientcache.provider.redis.RedisAdapter;

public class ProviderResolver {

    private final CaffeineAdapter caffeineAdapter;
    private final RedisAdapter redisAdapter;
    private final MultiLevelCacheAdapter multiLevelCacheAdapter;

    public ProviderResolver(CaffeineAdapter caffeineAdapter, RedisAdapter redisAdapter, MultiLevelCacheAdapter multiLevelCacheAdapter) {
        this.caffeineAdapter = caffeineAdapter;
        this.redisAdapter = redisAdapter;
        this.multiLevelCacheAdapter = multiLevelCacheAdapter;
    }

    /**
     * Resolves the active CachePort implementation based on the configured provider name.
     *
     * @param providerName configured provider name (caffeine, redis, multi-level)
     * @return the matching active CachePort bean
     * @throws ProviderNotConfiguredException if the provider name is unknown or if a requested adapter is null
     */
    public CachePort resolve(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new ProviderNotConfiguredException("No cache provider configured. Please set 'resilient-cache.provider'");
        }

        String target = providerName.trim().toLowerCase();
        switch (target) {
            case "caffeine":
                if (caffeineAdapter == null) {
                    throw new ProviderNotConfiguredException("Caffeine provider was selected but is not initialized.");
                }
                return caffeineAdapter;
            case "redis":
                if (redisAdapter == null) {
                    throw new ProviderNotConfiguredException("Redis provider was selected but is not initialized.");
                }
                return redisAdapter;
            case "multi-level":
                if (multiLevelCacheAdapter == null) {
                    throw new ProviderNotConfiguredException("Multi-level provider was selected but is not initialized.");
                }
                return multiLevelCacheAdapter;
            default:
                throw new ProviderNotConfiguredException("Unsupported cache provider value: '" + providerName 
                        + "'. Supported values are: caffeine, redis, multi-level");
        }
    }
}
