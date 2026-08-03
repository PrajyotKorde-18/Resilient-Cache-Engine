package com.resilientcache.provider.multi;

import com.resilientcache.port.CachePort;
import com.resilientcache.provider.caffeine.CaffeineAdapter;
import com.resilientcache.provider.redis.RedisAdapter;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MultiLevelCacheAdapter implements CachePort {

    private final CaffeineAdapter caffeineAdapter;
    private final RedisAdapter redisAdapter;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String instanceId;

    public MultiLevelCacheAdapter(CaffeineAdapter caffeineAdapter, 
                                  RedisAdapter redisAdapter, 
                                  RedisTemplate<String, Object> redisTemplate,
                                  String instanceId) {
        this.caffeineAdapter = caffeineAdapter;
        this.redisAdapter = redisAdapter;
        this.redisTemplate = redisTemplate;
        this.instanceId = instanceId;
    }

    @Override
    public Optional<Object> get(String key) {
        // Step 1: Query L1 (Caffeine)
        Optional<Object> l1Value = caffeineAdapter.get(key);
        if (l1Value.isPresent()) {
            return l1Value;
        }

        // Step 2: Query L2 (Redis) on L1 miss
        Optional<Object> l2Value = redisAdapter.get(key);
        if (l2Value.isPresent()) {
            Object value = l2Value.get();
            // Step 3: Back-propagate to L1 with remaining Redis TTL
            try {
                Long remainingTtlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (remainingTtlSeconds != null && remainingTtlSeconds > 0) {
                    caffeineAdapter.put(key, value, Duration.ofSeconds(remainingTtlSeconds));
                } else {
                    caffeineAdapter.put(key, value, null); // Fallback to default Caffeine TTL
                }
            } catch (Exception e) {
                // Degrade gracefully: still return the value even if back-propagation fails
                caffeineAdapter.put(key, value, null);
            }
            return Optional.of(value);
        }

        return Optional.empty();
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        // Write-through to both cache levels
        caffeineAdapter.put(key, value, ttl);
        redisAdapter.put(key, value, ttl);
        publishInvalidation(key);
    }

    @Override
    public void evict(String key) {
        caffeineAdapter.evict(key);
        redisAdapter.evict(key);
        publishInvalidation(key);
    }

    @Override
    public void clear() {
        caffeineAdapter.clear();
        redisAdapter.clear();
        publishInvalidation("CLEAR_ALL");
    }

    @Override
    public boolean healthCheck() {
        // Multi-level is considered fully healthy if both tiers are healthy
        return caffeineAdapter.healthCheck() && redisAdapter.healthCheck();
    }

    @Override
    public long getSize() {
        return redisAdapter.getSize();
    }

    private void publishInvalidation(String key) {
        try {
            if (redisAdapter.healthCheck()) {
                redisTemplate.convertAndSend("resilient-cache-invalidation", instanceId + ":" + key);
            }
        } catch (Exception e) {
            // Degrade silently if pub/sub publishing fails due to Redis offline
        }
    }
}
