package com.resilientcache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "resilient-cache")
public class CacheProperties {

    private boolean enabled = true;
    private String provider = "caffeine"; // default provider for M1
    private Duration defaultTtl = Duration.ofMinutes(30);
    private boolean stampedeProtection = true;
    private final Caffeine caffeine = new Caffeine();
    private final Redis redis = new Redis();
    private final Recovery recovery = new Recovery();
    private final Monitoring monitoring = new Monitoring();
    private final Warmup warmup = new Warmup();
    private final Dashboard dashboard = new Dashboard();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Duration getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(Duration defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    public boolean isStampedeProtection() {
        return stampedeProtection;
    }

    public void setStampedeProtection(boolean stampedeProtection) {
        this.stampedeProtection = stampedeProtection;
    }

    public Caffeine getCaffeine() {
        return caffeine;
    }

    public Redis getRedis() {
        return redis;
    }

    public Recovery getRecovery() {
        return recovery;
    }

    public Monitoring getMonitoring() {
        return monitoring;
    }

    public Warmup getWarmup() {
        return warmup;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public static class Caffeine {
        private long maximumSize = 10000;
        private Duration expireAfterWrite = Duration.ofMinutes(15);

        public long getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
        }

        public Duration getExpireAfterWrite() {
            return expireAfterWrite;
        }

        public void setExpireAfterWrite(Duration expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
        }
    }

    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private Duration timeout = Duration.ofSeconds(2);

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
    }

    public static class Recovery {
        private int retryMaxAttempts = 5;
        private Duration retryBackoff = Duration.ofSeconds(2);

        public int getRetryMaxAttempts() {
            return retryMaxAttempts;
        }

        public void setRetryMaxAttempts(int retryMaxAttempts) {
            this.retryMaxAttempts = retryMaxAttempts;
        }

        public Duration getRetryBackoff() {
            return retryBackoff;
        }

        public void setRetryBackoff(Duration retryBackoff) {
            this.retryBackoff = retryBackoff;
        }
    }

    public static class Monitoring {
        private Duration healthCheckInterval = Duration.ofSeconds(5);

        public Duration getHealthCheckInterval() {
            return healthCheckInterval;
        }

        public void setHealthCheckInterval(Duration healthCheckInterval) {
            this.healthCheckInterval = healthCheckInterval;
        }
    }

    public static class Warmup {
        private boolean enabled = true;
        private int topNKeys = 200;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getTopNKeys() {
            return topNKeys;
        }

        public void setTopNKeys(int topNKeys) {
            this.topNKeys = topNKeys;
        }
    }

    public static class Dashboard {
        private boolean enabled = true;
        private String path = "/dashboard";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
