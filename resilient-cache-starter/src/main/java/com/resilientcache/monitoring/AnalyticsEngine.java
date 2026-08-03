package com.resilientcache.monitoring;

import com.resilientcache.warmup.HotKeyRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AnalyticsEngine {

    private final ConcurrentHashMap<String, AccessStats> statsMap = new ConcurrentHashMap<>();

    /**
     * Records a database or cache access for a specific key.
     */
    public void recordAccess(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        statsMap.computeIfAbsent(key, AccessStats::new).recordAccess();
    }

    /**
     * Extracts and prunes the top hot keys sorted by their decay score.
     */
    public List<HotKeyRecord> extractTopHotKeys(int limit) {
        // Prune old inactive keys to prevent memory leaks
        statsMap.entrySet().removeIf(entry -> entry.getValue().calculateHotScore() < 0.05);

        return statsMap.values().stream()
                .map(stats -> new HotKeyRecord(stats.getKey(), stats.calculateHotScore(), stats.getLastAccessTime()))
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Clears all recorded statistics.
     */
    public void clear() {
        statsMap.clear();
    }
}
