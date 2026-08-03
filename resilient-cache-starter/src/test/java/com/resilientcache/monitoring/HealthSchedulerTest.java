package com.resilientcache.monitoring;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.event.HealthStatus;
import com.resilientcache.event.HealthStatusChangedEvent;
import com.resilientcache.event.ProviderFailureEvent;
import com.resilientcache.port.CachePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HealthSchedulerTest {

    private CachePort activeCachePort;
    private CacheProperties properties;
    private ApplicationEventPublisher eventPublisher;
    private HealthScheduler scheduler;

    @BeforeEach
    void setUp() {
        activeCachePort = mock(CachePort.class);
        properties = new CacheProperties();
        properties.setProvider("redis");
        eventPublisher = mock(ApplicationEventPublisher.class);
        scheduler = new HealthScheduler(activeCachePort, properties, eventPublisher);
    }

    @Test
    void testCheckHealthUpDoesNotPublishEventIfAlreadyUp() {
        when(activeCachePort.healthCheck()).thenReturn(true);

        scheduler.checkHealth();

        assertEquals(HealthStatus.UP, scheduler.getCurrentStatus());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void testCheckHealthTransitionsToDownAndPublishesEvents() {
        when(activeCachePort.healthCheck()).thenReturn(false);

        scheduler.checkHealth();

        assertEquals(HealthStatus.DOWN, scheduler.getCurrentStatus());

        // Verify status change and failure events are published
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        Object firstEvent = eventCaptor.getAllValues().get(0);
        Object secondEvent = eventCaptor.getAllValues().get(1);

        assertTrue(firstEvent instanceof HealthStatusChangedEvent);
        HealthStatusChangedEvent statusEvent = (HealthStatusChangedEvent) firstEvent;
        assertEquals(HealthStatus.UP, statusEvent.getPreviousStatus());
        assertEquals(HealthStatus.DOWN, statusEvent.getNewStatus());
        assertEquals("redis", statusEvent.getProviderName());

        assertTrue(secondEvent instanceof ProviderFailureEvent);
        ProviderFailureEvent failureEvent = (ProviderFailureEvent) secondEvent;
        assertEquals("redis", failureEvent.getProviderName());
    }

    @Test
    void testCheckHealthTransitionsBackToUp() {
        // Step 1: Transition to DOWN
        when(activeCachePort.healthCheck()).thenReturn(false);
        scheduler.checkHealth();
        assertEquals(HealthStatus.DOWN, scheduler.getCurrentStatus());
        reset(eventPublisher);

        // Step 2: Transition back to UP
        when(activeCachePort.healthCheck()).thenReturn(true);
        scheduler.checkHealth();
        assertEquals(HealthStatus.UP, scheduler.getCurrentStatus());

        // Verify status transition event is published
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        Object event = eventCaptor.getValue();
        assertTrue(event instanceof HealthStatusChangedEvent);
        HealthStatusChangedEvent statusEvent = (HealthStatusChangedEvent) event;
        assertEquals(HealthStatus.DOWN, statusEvent.getPreviousStatus());
        assertEquals(HealthStatus.UP, statusEvent.getNewStatus());
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Condition is false");
        }
    }
}
