package com.resilientcache.recovery;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.event.ProviderFailureEvent;
import com.resilientcache.event.RecoveryCompletedEvent;
import com.resilientcache.event.RecoveryStartedEvent;
import com.resilientcache.exception.RecoveryFailedException;
import com.resilientcache.port.CachePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecoveryManagerTest {

    private CachePort activeCachePort;
    private CacheProperties properties;
    private ApplicationEventPublisher eventPublisher;
    private RecoveryManager manager;

    @BeforeEach
    void setUp() {
        activeCachePort = mock(CachePort.class);
        properties = new CacheProperties();
        properties.setProvider("redis");
        properties.getRecovery().setRetryMaxAttempts(3);
        properties.getRecovery().setRetryBackoff(Duration.ofMillis(2)); // Fast wait time for test run
        eventPublisher = mock(ApplicationEventPublisher.class);
        manager = new RecoveryManager(activeCachePort, properties, eventPublisher);
    }

    @Test
    void testRecoverySucceedsImmediately() {
        when(activeCachePort.healthCheck()).thenReturn(true);

        manager.onProviderFailure(new ProviderFailureEvent("redis", "test failure"));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        assertTrue(eventCaptor.getAllValues().get(0) instanceof RecoveryStartedEvent);
        assertTrue(eventCaptor.getAllValues().get(1) instanceof RecoveryCompletedEvent);

        RecoveryCompletedEvent completedEvent = (RecoveryCompletedEvent) eventCaptor.getAllValues().get(1);
        assertEquals("redis", completedEvent.getProviderName());
        assertEquals(1, completedEvent.getRetryAttempts());
    }

    @Test
    void testRecoverySucceedsAfterRetries() {
        // 1st connection check fails, 2nd succeeds
        when(activeCachePort.healthCheck()).thenReturn(false, true);

        manager.onProviderFailure(new ProviderFailureEvent("redis", "test failure"));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        RecoveryCompletedEvent completedEvent = (RecoveryCompletedEvent) eventCaptor.getAllValues().get(1);
        assertEquals("redis", completedEvent.getProviderName());
        assertEquals(2, completedEvent.getRetryAttempts());
    }

    @Test
    void testRecoveryFailsAfterExhaustingRetries() {
        // All pings fail
        when(activeCachePort.healthCheck()).thenReturn(false);

        assertThrows(RecoveryFailedException.class, () -> {
            manager.onProviderFailure(new ProviderFailureEvent("redis", "test failure"));
        });

        // Started event is sent, but completed event is NEVER sent
        verify(eventPublisher, times(1)).publishEvent(any(RecoveryStartedEvent.class));
        verify(eventPublisher, never()).publishEvent(any(RecoveryCompletedEvent.class));
    }
}
