package com.resilientcache.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsTracker {

    private final MeterRegistry meterRegistry;

    private final AtomicLong l2HitCount = new AtomicLong(0);
    private final AtomicLong l2MissCount = new AtomicLong(0);

    private final AtomicLong totalReadTimeNs = new AtomicLong(0);
    private final AtomicLong totalReadCount = new AtomicLong(0);

    private final AtomicLong totalWriteTimeNs = new AtomicLong(0);
    private final AtomicLong totalWriteCount = new AtomicLong(0);

    public MetricsTracker() {
        this.meterRegistry = null;
    }

    public MetricsTracker(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        registerMetrics();
    }

    private void registerMetrics() {
        if (meterRegistry != null) {
            meterRegistry.gauge("resilient.cache.l2.hits", l2HitCount);
            meterRegistry.gauge("resilient.cache.l2.misses", l2MissCount);
            meterRegistry.gauge("resilient.cache.latency.read.average", this, MetricsTracker::getAverageReadLatencyMs);
            meterRegistry.gauge("resilient.cache.latency.write.average", this, MetricsTracker::getAverageWriteLatencyMs);
        }
    }

    public void incrementL2Hit() {
        l2HitCount.incrementAndGet();
        if (meterRegistry != null) {
            meterRegistry.counter("resilient.cache.hits", "provider", "redis").increment();
        }
    }

    public void incrementL2Miss() {
        l2MissCount.incrementAndGet();
        if (meterRegistry != null) {
            meterRegistry.counter("resilient.cache.misses", "provider", "redis").increment();
        }
    }

    public void recordReadLatency(long latencyNs) {
        totalReadTimeNs.addAndGet(latencyNs);
        totalReadCount.incrementAndGet();
    }

    public void recordWriteLatency(long latencyNs) {
        totalWriteTimeNs.addAndGet(latencyNs);
        totalWriteCount.incrementAndGet();
    }

    public long getL2HitCount() {
        return l2HitCount.get();
    }

    public long getL2MissCount() {
        return l2MissCount.get();
    }

    public double getAverageReadLatencyMs() {
        long count = totalReadCount.get();
        if (count == 0) {
            return 0.0;
        }
        return (totalReadTimeNs.get() / (double) count) / 1_000_000.0;
    }

    public double getAverageWriteLatencyMs() {
        long count = totalWriteCount.get();
        if (count == 0) {
            return 0.0;
        }
        return (totalWriteTimeNs.get() / (double) count) / 1_000_000.0;
    }

    public void reset() {
        l2HitCount.set(0);
        l2MissCount.set(0);
        totalReadTimeNs.set(0);
        totalReadCount.set(0);
        totalWriteTimeNs.set(0);
        totalWriteCount.set(0);
    }
}
