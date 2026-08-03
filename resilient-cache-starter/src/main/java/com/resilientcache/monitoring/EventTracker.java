package com.resilientcache.monitoring;

import com.resilientcache.event.*;
import org.springframework.context.event.EventListener;

import java.text.SimpleDateFormat;
import java.util.*;

public class EventTracker {

    private final List<Map<String, Object>> eventsList = Collections.synchronizedList(new LinkedList<>());
    private static final int MAX_EVENTS = 50;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @EventListener
    public void onHealthStatusChanged(HealthStatusChangedEvent event) {
        addEvent(
            "HEALTH_CHANGE", 
            event.getProviderName(),
            String.format("Health status transitioned from %s to %s", event.getPreviousStatus(), event.getNewStatus()),
            event.getTimestamp()
        );
    }

    @EventListener
    public void onProviderFailure(ProviderFailureEvent event) {
        addEvent(
            "FAILURE", 
            event.getProviderName(),
            String.format("Outage detected: %s", event.getReason()),
            event.getTimestamp()
        );
    }

    @EventListener
    public void onRecoveryStarted(RecoveryStartedEvent event) {
        addEvent(
            "RECOVERY_START", 
            event.getProviderName(),
            "Connection recovery task initiated",
            event.getTimestamp()
        );
    }

    @EventListener
    public void onRecoveryCompleted(RecoveryCompletedEvent event) {
        double durationSec = event.getDurationMs() / 1000.0;
        addEvent(
            "RECOVERY_SUCCESS", 
            event.getProviderName(),
            String.format("Reconnected successfully | Attempts=%d | Duration=%.2fs", event.getRetryAttempts(), durationSec),
            event.getTimestamp()
        );
    }

    @EventListener
    public void onWarmupCompleted(WarmupCompletedEvent event) {
        double durationSec = event.getDurationMs() / 1000.0;
        addEvent(
            "WARMUP_SUCCESS", 
            event.getProviderName(),
            String.format("Preloading warm-up complete | Loaded=%d keys | Duration=%.2fs", event.getKeysLoaded(), durationSec),
            event.getTimestamp()
        );
    }

    public void addManualEvent(String type, String provider, String message) {
        addEvent(type, provider, message, System.currentTimeMillis());
    }

    private void addEvent(String type, String provider, String message, long timestamp) {
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("type", type);
        eventMap.put("provider", provider);
        eventMap.put("message", message);
        eventMap.put("timestamp", dateFormat.format(new Date(timestamp)));
        
        eventsList.add(0, eventMap); // Add to head so latest shows first

        while (eventsList.size() > MAX_EVENTS) {
            eventsList.remove(eventsList.size() - 1);
        }
    }

    public List<Map<String, Object>> getEvents() {
        return new ArrayList<>(eventsList);
    }

    public void clear() {
        eventsList.clear();
    }
}
