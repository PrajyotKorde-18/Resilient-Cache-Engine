package com.resilientcache.warmup;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.event.RecoveryCompletedEvent;
import com.resilientcache.port.CachePort;
import com.resilientcache.port.CacheWarmupLoader;
import com.resilientcache.port.HotKeyPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

class WarmupServiceTest {

    private CachePort activeCachePort;
    private CacheProperties properties;
    private ObjectProvider<HotKeyPort> hotKeyPortProvider;
    private HotKeyPort hotKeyPort;
    private CacheWarmupLoader productLoader;
    private CacheWarmupLoader customerLoader;
    private ApplicationEventPublisher eventPublisher;
    private WarmupService warmupService;

    @BeforeEach
    void setUp() {
        activeCachePort = mock(CachePort.class);
        properties = new CacheProperties();
        properties.getWarmup().setEnabled(true);
        
        hotKeyPort = mock(HotKeyPort.class);
        hotKeyPortProvider = mock(ObjectProvider.class);
        when(hotKeyPortProvider.getIfAvailable()).thenReturn(hotKeyPort);
        
        productLoader = mock(CacheWarmupLoader.class);
        when(productLoader.supports("products")).thenReturn(true);
        when(productLoader.load("products:100")).thenReturn("mock-product-value");
        
        customerLoader = mock(CacheWarmupLoader.class);
        when(customerLoader.supports("customers")).thenReturn(true);
        when(customerLoader.load("customers:15")).thenReturn("mock-customer-value");
        
        List<CacheWarmupLoader> loaders = Arrays.asList(productLoader, customerLoader);
        eventPublisher = mock(ApplicationEventPublisher.class);
        
        warmupService = new WarmupService(activeCachePort, properties, hotKeyPortProvider, loaders, eventPublisher);
    }

    @Test
    void testWarmupPreloadsKeysOnRecoveryEvent() {
        List<HotKeyRecord> hotKeys = Arrays.asList(
            new HotKeyRecord("products:100", 10.0, System.currentTimeMillis()),
            new HotKeyRecord("customers:15", 5.0, System.currentTimeMillis())
        );
        when(hotKeyPort.getTopHotKeys(anyInt())).thenReturn(hotKeys);
        
        RecoveryCompletedEvent event = new RecoveryCompletedEvent("redis", 1, 100);

        warmupService.onRecoveryCompleted(event);

        verify(activeCachePort, times(1)).put(eq("products:100"), eq("mock-product-value"), eq(properties.getDefaultTtl()));
        verify(activeCachePort, times(1)).put(eq("customers:15"), eq("mock-customer-value"), eq(properties.getDefaultTtl()));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void testWarmupDoesNotTriggerIfDisabled() {
        properties.getWarmup().setEnabled(false);
        RecoveryCompletedEvent event = new RecoveryCompletedEvent("redis", 1, 100);

        warmupService.onRecoveryCompleted(event);

        verifyNoInteractions(activeCachePort);
    }
}
