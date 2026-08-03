package com.resilientcache.config;

import com.resilientcache.aspect.CacheAspect;
import com.resilientcache.controller.CacheMonitorController;
import com.resilientcache.core.CacheEngine;
import com.resilientcache.core.CacheKeyGenerator;
import com.resilientcache.core.CacheManager;
import com.resilientcache.core.ProviderResolver;
import com.resilientcache.monitoring.EventTracker;
import com.resilientcache.monitoring.HealthScheduler;
import com.resilientcache.monitoring.MetricsTracker;
import com.resilientcache.port.CachePort;
import com.resilientcache.provider.caffeine.CaffeineAdapter;
import com.resilientcache.provider.multi.MultiLevelCacheAdapter;
import com.resilientcache.provider.redis.RedisAdapter;
import com.resilientcache.recovery.RecoveryManager;
import com.resilientcache.warmup.WarmupService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnProperty(prefix = "resilient-cache", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class ResilientCacheAutoConfiguration {


    // --- Async Task Executor for Recovery & Warmup ---
    @Bean(name = "resilientCacheTaskExecutor")
    public Executor resilientCacheTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("cache-resilience-");
        executor.initialize();
        return executor;
    }

    // --- Monitoring & Metrics Trackers ---
    @Bean
    @ConditionalOnMissingBean
    public MetricsTracker metricsTracker(org.springframework.beans.factory.ObjectProvider<io.micrometer.core.instrument.MeterRegistry> meterRegistryProvider) {
        io.micrometer.core.instrument.MeterRegistry registry = meterRegistryProvider.getIfAvailable();
        if (registry != null) {
            return new MetricsTracker(registry);
        }
        return new MetricsTracker();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventTracker eventTracker() {
        return new EventTracker();
    }

    // --- Redis Infrastructure ---
    @Bean
    @ConditionalOnMissingBean(name = "resilientCacheRedisTemplate")
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> resilientCacheRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    // --- Individual Adapters ---
    @Bean
    @ConditionalOnMissingBean
    public CaffeineAdapter caffeineAdapter(CacheProperties properties) {
        return new CaffeineAdapter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "resilientCacheRedisTemplate")
    public RedisAdapter redisAdapter(
            RedisTemplate<String, Object> resilientCacheRedisTemplate, 
            CacheProperties properties,
            MetricsTracker metricsTracker) {
        return new RedisAdapter(resilientCacheRedisTemplate, properties, metricsTracker);
    }

    @Bean
    public String resilientCacheInstanceId() {
        return java.util.UUID.randomUUID().toString();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({CaffeineAdapter.class, RedisAdapter.class, RedisTemplate.class})
    public MultiLevelCacheAdapter multiLevelCacheAdapter(
            CaffeineAdapter caffeineAdapter, 
            RedisAdapter redisAdapter, 
            RedisTemplate<String, Object> resilientCacheRedisTemplate,
            String resilientCacheInstanceId) {
        return new MultiLevelCacheAdapter(caffeineAdapter, redisAdapter, resilientCacheRedisTemplate, resilientCacheInstanceId);
    }

    @Bean
    @ConditionalOnBean({RedisConnectionFactory.class, CaffeineAdapter.class})
    public org.springframework.data.redis.listener.RedisMessageListenerContainer resilientCacheMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        
        org.springframework.data.redis.listener.RedisMessageListenerContainer container = 
                new org.springframework.data.redis.listener.RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    @ConditionalOnBean(org.springframework.data.redis.listener.RedisMessageListenerContainer.class)
    public Object resilientCacheMessageListenerContainerStarter(
            org.springframework.data.redis.listener.RedisMessageListenerContainer container,
            CaffeineAdapter caffeineAdapter,
            String resilientCacheInstanceId,
            @Autowired(required = false) RedisAdapter redisAdapter) {
        
        return new Object() {
            private boolean subscribed = false;

            private final org.springframework.data.redis.connection.MessageListener listener = (message, pattern) -> {
                try {
                    String body = new String(message.getBody(), java.nio.charset.StandardCharsets.UTF_8);
                    // Unwrap Jackson serialization double quotes if present
                    if (body.startsWith("\"") && body.endsWith("\"")) {
                        body = body.substring(1, body.length() - 1);
                    }
                    int separatorIdx = body.indexOf(':');
                    if (separatorIdx > 0) {
                        String senderId = body.substring(0, separatorIdx);
                        String key = body.substring(separatorIdx + 1);
                        
                        if (!senderId.equals(resilientCacheInstanceId)) {
                            if ("CLEAR_ALL".equals(key)) {
                                caffeineAdapter.clear();
                            } else {
                                caffeineAdapter.evict(key);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore decoding exceptions silently
                }
            };

            private synchronized void subscribeIfHealthy() {
                if (redisAdapter != null) {
                    try {
                        if (redisAdapter.healthCheck()) {
                            if (!subscribed) {
                                container.addMessageListener(listener, 
                                        new org.springframework.data.redis.listener.ChannelTopic("resilient-cache-invalidation"));
                                subscribed = true;
                            }
                            if (!container.isRunning()) {
                                container.start();
                            }
                        }
                    } catch (Exception e) {
                        // Degrade silently
                    }
                }
            }

            @org.springframework.context.event.EventListener
            public void onApplicationReady(org.springframework.boot.context.event.ApplicationReadyEvent event) {
                subscribeIfHealthy();
            }

            @org.springframework.context.event.EventListener
            public void onHealthStatusChanged(com.resilientcache.event.HealthStatusChangedEvent event) {
                if (event.getNewStatus() == com.resilientcache.event.HealthStatus.UP) {
                    subscribeIfHealthy();
                }
            }

            @org.springframework.context.event.EventListener
            public void onRecoveryCompleted(com.resilientcache.event.RecoveryCompletedEvent event) {
                subscribeIfHealthy();
            }
        };
    }

    // --- Provider Resolution ---
    @Bean
    @ConditionalOnMissingBean
    public ProviderResolver providerResolver(
            CaffeineAdapter caffeineAdapter,
            RedisAdapter redisAdapter,
            MultiLevelCacheAdapter multiLevelCacheAdapter) {
        return new ProviderResolver(caffeineAdapter, redisAdapter, multiLevelCacheAdapter);
    }

    @Bean
    @Primary
    public CachePort activeCachePort(ProviderResolver providerResolver, CacheProperties properties) {
        return providerResolver.resolve(properties.getProvider());
    }

    // --- Core Beans ---
    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager(CachePort activeCachePort) {
        return new CacheManager(activeCachePort);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheKeyGenerator cacheKeyGenerator() {
        return new CacheKeyGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheEngine cacheEngine(CacheManager cacheManager, CacheProperties properties, MetricsTracker metricsTracker) {
        return new CacheEngine(cacheManager, properties, metricsTracker);
    }

    @Bean
    @ConditionalOnMissingBean
    public com.resilientcache.monitoring.AnalyticsEngine analyticsEngine() {
        return new com.resilientcache.monitoring.AnalyticsEngine();
    }

    @Bean
    @ConditionalOnMissingBean
    public com.resilientcache.monitoring.HotKeyScheduler hotKeyScheduler(
            com.resilientcache.monitoring.AnalyticsEngine analyticsEngine,
            org.springframework.beans.factory.ObjectProvider<com.resilientcache.port.HotKeyPort> hotKeyPortProvider,
            CacheProperties properties) {
        return new com.resilientcache.monitoring.HotKeyScheduler(analyticsEngine, hotKeyPortProvider, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheAspect cacheAspect(
            CacheEngine cacheEngine, 
            CacheKeyGenerator cacheKeyGenerator, 
            CacheProperties properties,
            org.springframework.beans.factory.ObjectProvider<com.resilientcache.monitoring.AnalyticsEngine> analyticsEngineProvider) {
        return new CacheAspect(cacheEngine, cacheKeyGenerator, properties, analyticsEngineProvider);
    }

    // --- Resilience & Self-Healing Beans ---
    @Bean
    @ConditionalOnMissingBean
    public HealthScheduler healthScheduler(CachePort activeCachePort, CacheProperties properties, ApplicationEventPublisher eventPublisher) {
        return new HealthScheduler(activeCachePort, properties, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public RecoveryManager recoveryManager(CachePort activeCachePort, CacheProperties properties, ApplicationEventPublisher eventPublisher) {
        return new RecoveryManager(activeCachePort, properties, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public WarmupService warmupService(
            CachePort activeCachePort, 
            CacheProperties properties,
            org.springframework.beans.factory.ObjectProvider<com.resilientcache.port.HotKeyPort> hotKeyPortProvider,
            java.util.List<com.resilientcache.port.CacheWarmupLoader> warmupLoaders,
            ApplicationEventPublisher eventPublisher) {
        return new WarmupService(activeCachePort, properties, hotKeyPortProvider, warmupLoaders, eventPublisher);
    }

    // --- Dashboard & Monitoring Rest Endpoint ---
    @Bean
    @ConditionalOnMissingBean
    public CacheMonitorController cacheMonitorController(
            CaffeineAdapter caffeineAdapter,
            @Autowired(required = false) RedisAdapter redisAdapter,
            MetricsTracker metricsTracker,
            EventTracker eventTracker,
            CacheManager cacheManager,
            CacheProperties properties,
            HealthScheduler healthScheduler,
            com.resilientcache.monitoring.AnalyticsEngine analyticsEngine) {
        return new CacheMonitorController(
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
}
