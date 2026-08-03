package com.resilientcache.monitoring;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.event.HealthStatus;
import com.resilientcache.event.HealthStatusChangedEvent;
import com.resilientcache.event.ProviderFailureEvent;
import com.resilientcache.port.CachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

public class HealthScheduler {

    private static final Logger log = LoggerFactory.getLogger(HealthScheduler.class);

    private final CachePort activeCachePort;
    private final CacheProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    private volatile HealthStatus currentStatus = HealthStatus.UP;

    public HealthScheduler(CachePort activeCachePort, CacheProperties properties, ApplicationEventPublisher eventPublisher) {
        this.activeCachePort = activeCachePort;
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Periodically queries the active cache provider's health and publishes transition events on changes.
     */
    @Scheduled(fixedDelayString = "${resilient-cache.monitoring.health-check-interval:5000}")
    public void checkHealth() {
        String providerName = properties.getProvider().substring(0, 1).toUpperCase() 
                + properties.getProvider().substring(1).toLowerCase();
        
        boolean isHealthy = false;
        try {
            isHealthy = activeCachePort.healthCheck();
        } catch (Exception e) {
            // Assume unhealthy on exception
        }

        HealthStatus newStatus = isHealthy ? HealthStatus.UP : HealthStatus.DOWN;
        HealthStatus previousStatus = this.currentStatus;

        if (newStatus != previousStatus) {
            this.currentStatus = newStatus;
            
            // Publish status changed event
            eventPublisher.publishEvent(new HealthStatusChangedEvent(properties.getProvider(), previousStatus, newStatus));

            if (newStatus == HealthStatus.DOWN) {
                log.warn("{} Health Check Failed | Status transitioned to DOWN", providerName);
                // Publish provider failure event to trigger automated recovery manager
                eventPublisher.publishEvent(new ProviderFailureEvent(properties.getProvider(), "Health check ping failed"));
            } else {
                log.info("{} Health Check Restored | Status transitioned to UP", providerName);
            }
        }
    }

    public HealthStatus getCurrentStatus() {
        return currentStatus;
    }
}
