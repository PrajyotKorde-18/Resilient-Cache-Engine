package com.resilientcache.aspect;

import com.resilientcache.annotation.ResilientCache;
import com.resilientcache.config.CacheProperties;
import com.resilientcache.core.CacheEngine;
import com.resilientcache.core.CacheKeyGenerator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.time.Duration;

@Aspect
public class CacheAspect {

    private final CacheEngine cacheEngine;
    private final CacheKeyGenerator keyGenerator;
    private final CacheProperties properties;
    private final org.springframework.beans.factory.ObjectProvider<com.resilientcache.monitoring.AnalyticsEngine> analyticsEngineProvider;

    public CacheAspect(CacheEngine cacheEngine, 
                       CacheKeyGenerator keyGenerator, 
                       CacheProperties properties,
                       org.springframework.beans.factory.ObjectProvider<com.resilientcache.monitoring.AnalyticsEngine> analyticsEngineProvider) {
        this.cacheEngine = cacheEngine;
        this.keyGenerator = keyGenerator;
        this.properties = properties;
        this.analyticsEngineProvider = analyticsEngineProvider;
    }

    @Around("@annotation(resilientCache)")
    public Object handleCachedMethod(ProceedingJoinPoint joinPoint, ResilientCache resilientCache) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Generate the unique cache key
        String cacheKey = keyGenerator.generateKey(
                resilientCache.name(), 
                resilientCache.key(), 
                method, 
                joinPoint.getArgs(), 
                joinPoint.getTarget()
        );

        // Resolve optional TTL override
        Duration ttlOverride = parseDuration(resilientCache.ttl());

        // Delegate to CacheEngine using the Cache-Aside pattern loader
        Object result = cacheEngine.get(cacheKey, () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }
                throw new RuntimeException("Error executing cached method target", throwable);
            }
        }, ttlOverride);

        if (result != null) {
            com.resilientcache.monitoring.AnalyticsEngine analyticsEngine = analyticsEngineProvider.getIfAvailable();
            if (analyticsEngine != null) {
                analyticsEngine.recordAccess(cacheKey);
            }
        }

        return result;
    }

    private Duration parseDuration(String ttlStr) {
        if (ttlStr == null || ttlStr.trim().isEmpty()) {
            return null;
        }
        String s = ttlStr.trim().toLowerCase();
        try {
            if (s.endsWith("ms")) {
                return Duration.ofMillis(Long.parseLong(s.substring(0, s.length() - 2)));
            } else if (s.endsWith("s")) {
                return Duration.ofSeconds(Long.parseLong(s.substring(0, s.length() - 1)));
            } else if (s.endsWith("m")) {
                return Duration.ofMinutes(Long.parseLong(s.substring(0, s.length() - 1)));
            } else if (s.endsWith("h")) {
                return Duration.ofHours(Long.parseLong(s.substring(0, s.length() - 1)));
            } else if (s.endsWith("d")) {
                return Duration.ofDays(Long.parseLong(s.substring(0, s.length() - 1)));
            }
            return Duration.parse(ttlStr); // standard ISO-8601 fallback
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid TTL duration override: " + ttlStr, e);
        }
    }
}
