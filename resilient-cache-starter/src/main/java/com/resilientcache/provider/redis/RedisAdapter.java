package com.resilientcache.provider.redis;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.port.CachePort;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public class RedisAdapter implements CachePort {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties properties;
    private final com.resilientcache.monitoring.MetricsTracker metricsTracker;

    public RedisAdapter(RedisTemplate<String, Object> redisTemplate, CacheProperties properties, com.resilientcache.monitoring.MetricsTracker metricsTracker) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.metricsTracker = metricsTracker;
    }

    @Override
    public Optional<Object> get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (metricsTracker != null) {
                if (value != null) {
                    metricsTracker.incrementL2Hit();
                } else {
                    metricsTracker.incrementL2Miss();
                }
            }
            return Optional.ofNullable(value);
        } catch (Exception e) {
            if (metricsTracker != null) {
                metricsTracker.incrementL2Miss();
            }
            // Log warning or degrade gracefully
            return Optional.empty();
        }
    }


    @Override
    public void put(String key, Object value, Duration ttl) {
        if (value == null) {
            return;
        }
        try {
            Duration effectiveTtl = ttl != null ? ttl : properties.getDefaultTtl();
            redisTemplate.opsForValue().set(key, value, effectiveTtl);
        } catch (Exception e) {
            // Log warning or degrade gracefully
        }
    }

    @Override
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            // Log warning
        }
    }

    @Override
    public void clear() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            // Log warning
        }
    }

    @Override
    public boolean healthCheck() {
        if (redisTemplate.getConnectionFactory() == null) {
            return false;
        }
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            String pingResult = connection.ping();
            return "PONG".equalsIgnoreCase(pingResult);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long getSize() {
        if (redisTemplate.getConnectionFactory() == null) {
            return 0;
        }
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            Long size = connection.dbSize();
            return size != null ? size : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
