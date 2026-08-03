package com.resilientcache.recovery;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.event.ProviderFailureEvent;
import com.resilientcache.event.RecoveryCompletedEvent;
import com.resilientcache.event.RecoveryStartedEvent;
import com.resilientcache.exception.RecoveryFailedException;
import com.resilientcache.port.CachePort;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.time.Duration;

public class RecoveryManager {

    private static final Logger log = LoggerFactory.getLogger(RecoveryManager.class);

    private final CachePort activeCachePort;
    private final CacheProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    private volatile boolean recoveryInProgress = false;

    public RecoveryManager(CachePort activeCachePort, CacheProperties properties, ApplicationEventPublisher eventPublisher) {
        this.activeCachePort = activeCachePort;
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Listens asynchronously to provider outages and begins the self-healing reconnection retry loop.
     */
    @EventListener
    @Async("resilientCacheTaskExecutor")
    public void onProviderFailure(ProviderFailureEvent event) {
        if (recoveryInProgress) {
            log.debug("Recovery already in progress for provider: {}. Skipping.", event.getProviderName());
            return;
        }

        recoveryInProgress = true;
        String rawProvider = event.getProviderName();
        String providerName = rawProvider.substring(0, 1).toUpperCase() + rawProvider.substring(1).toLowerCase();

        log.info("Recovery Started for provider: {}", providerName);
        eventPublisher.publishEvent(new RecoveryStartedEvent(rawProvider));

        long startTime = System.currentTimeMillis();

        // Configure Resilience4j Retry with exponential backoff
        long initialDelayMs = properties.getRecovery().getRetryBackoff().toMillis();
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(properties.getRecovery().getRetryMaxAttempts())
                .intervalFunction(IntervalFunction.ofExponentialBackoff(initialDelayMs, 1.5))
                .build();

        RetryRegistry registry = RetryRegistry.of(retryConfig);
        Retry retry = registry.retry(rawProvider + "-recovery");

        // Log retry warnings matching specifications
        retry.getEventPublisher().onRetry(retryEvent -> {
            log.warn("{} Health Check Failed | Retry={}", providerName, retryEvent.getNumberOfRetryAttempts());
        });

        try {
            // Run recovery connection checks inside the retry block
            Retry.decorateRunnable(retry, () -> {
                boolean check = activeCachePort.healthCheck();
                if (!check) {
                    throw new RuntimeException("Cache provider still unresponsive");
                }
            }).run();

            long durationMs = System.currentTimeMillis() - startTime;
            int totalAttempts = (int) (retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt() 
                    + retry.getMetrics().getNumberOfFailedCallsWithRetryAttempt() + 1);


            log.info("Recovery Completed | Duration={}s", String.format("%.1f", durationMs / 1000.0));
            eventPublisher.publishEvent(new RecoveryCompletedEvent(rawProvider, totalAttempts, durationMs));

        } catch (Exception e) {
            log.error("Recovery Failed for provider: {} after exhausting retry budget.", providerName);
            throw new RecoveryFailedException("Failed to restore connection to provider: " + rawProvider, e);
        } finally {
            recoveryInProgress = false;
        }
    }

    public boolean isRecoveryInProgress() {
        return recoveryInProgress;
    }
}
