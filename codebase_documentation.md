# Resilient Cache Engine: Complete Codebase & Plain English Documentation

This document lists all source files implemented for the **Resilient Self-Healing Cache Engine** project (Milestones 1 to 6) and provides plain-English explanations for what each file does and why it was designed that way.

---

## Root Maven Project POM

### File Path: `pom.xml`

### 💡 What this code does (in simple language):
This is the parent Maven Configuration file. It governs the build process for the entire project, links the submodules together (resilient-cache-starter and resilient-cache-demo), and locks down the versions of external libraries like Spring Boot (3.3.4), Caffeine (3.1.8), and Resilience4j (2.2.0) so everything remains compatible.

### 💻 Source Code:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.resilientcache</groupId>
    <artifactId>resilient-cache-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>

    <name>Resilient Cache Engine Parent</name>
    <description>Parent POM for Resilient Self-Healing Cache Engine for Spring Boot</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <java.version>21</java.version>
        <caffeine.version>3.1.8</caffeine.version>
        <resilient-cache.version>1.0.0</resilient-cache.version>
        <resilience4j.version>2.2.0</resilience4j.version>
    </properties>

    <modules>
        <module>resilient-cache-starter</module>
        <module>resilient-cache-demo</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <!-- Project Modules -->
            <dependency>
                <groupId>com.resilientcache</groupId>
                <artifactId>resilient-cache-starter</artifactId>
                <version>${resilient-cache.version}</version>
            </dependency>
            
            <!-- Caching Providers -->
            <dependency>
                <groupId>com.github.ben-manes.caffeine</groupId>
                <artifactId>caffeine</artifactId>
                <version>${caffeine.version}</version>
            </dependency>

            <!-- Resilience4j Retry -->
            <dependency>
                <groupId>io.github.resilience4j</groupId>
                <artifactId>resilience4j-retry</artifactId>
                <version>${resilience4j.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

```

---

## Starter Module POM

### File Path: `resilient-cache-starter/pom.xml`

### 💡 What this code does (in simple language):
This POM file defines the dependencies required specifically by the core starter middleware. It imports Spring Boot core, AOP (for annotation interception), Caffeine (for L1 cache), Spring Data Redis (for L2 cache), Resilience4j Retry (for connection recovery), Spring Boot Starter Web (for endpoint mapping), and Actuator/Micrometer core dependencies for statistics.

### 💻 Source Code:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.resilientcache</groupId>
        <artifactId>resilient-cache-parent</artifactId>
        <version>1.0.0</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>resilient-cache-starter</artifactId>
    <name>Resilient Cache Engine Starter</name>
    <description>Core library and auto-configuration for Resilient Self-Healing Cache Engine</description>

    <dependencies>
        <!-- Spring Boot Core Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <!-- Spring AOP for Aspect-based `@ResilientCache` -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <!-- Caffeine Cache for L1 caching -->
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
        </dependency>

        <!-- Spring Boot Starter Data Redis for L2 caching (uses Lettuce by default) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- Resilience4j Retry for connection recovery -->
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-retry</artifactId>
        </dependency>

        <!-- Spring Boot Starter Web for REST endpoints and dashboard -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>        <!-- Spring Boot Starter Actuator for metrics and monitoring -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Spring Boot Configuration Processor (metadata for IDE autocomplete) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>

```

---

## Demo Module POM

### File Path: `resilient-cache-demo/pom.xml`

### 💡 What this code does (in simple language):
This POM configuration manages the demo application. It lists the core starter library (resilient-cache-starter), Spring Web starter, Spring Data JPA (for Hibernate queries), PostgreSQL JDBC driver, Actuator monitoring, and test tools like in-memory H2 database.

### 💻 Source Code:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.resilientcache</groupId>
        <artifactId>resilient-cache-parent</artifactId>
        <version>1.0.0</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>resilient-cache-demo</artifactId>
    <name>Resilient Cache Engine Demo</name>
    <description>Demo application evaluating the Resilient Cache Starter</description>

    <dependencies>
        <!-- Resilient Cache Starter -->
        <dependency>
            <groupId>com.resilientcache</groupId>
            <artifactId>resilient-cache-starter</artifactId>
        </dependency>

        <!-- Spring Web Starter for REST endpoints -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA for persistence mapping -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- PostgreSQL JDBC Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>

        <!-- Spring Boot Actuator for health check exposure -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Test Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- In-memory H2 database for unit and integration testing -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>


    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>

```

---

## CacheProperties.java (Configuration Properties)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/config/CacheProperties.java`

### 💡 What this code does (in simple language):
This class maps properties specified in 'application.yml' under the prefix 'resilient-cache' directly to Java objects. It handles variables like whether the cache is enabled, the active provider ('caffeine', 'redis', or 'multi-level'), default TTL durations, and custom settings for Caffeine, Redis, monitoring, and recovery backoffs.

### 💻 Source Code:
```java
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

```

---

## ResilientCacheAutoConfiguration.java (Spring Boot Auto-Configuration)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/config/ResilientCacheAutoConfiguration.java`

### 💡 What this code does (in simple language):
This is the core bootstrapping configuration class of our library. It is annotated with @EnableScheduling and @EnableAsync. It registers an asynchronous thread pool executor bean (resilientCacheTaskExecutor), Lettuce connection factories, RedisTemplate configuration, the active cache adapter, resilience beans (HealthScheduler, RecoveryManager, and WarmupService), and dashboard beans (MetricsTracker, EventTracker, and CacheMonitorController), passing Micrometer MeterRegistry to metrics tracking.

### 💻 Source Code:
```java
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
    @ConditionalOnMissingBean
    @ConditionalOnBean({CaffeineAdapter.class, RedisAdapter.class, RedisTemplate.class})
    public MultiLevelCacheAdapter multiLevelCacheAdapter(
            CaffeineAdapter caffeineAdapter, 
            RedisAdapter redisAdapter, 
            RedisTemplate<String, Object> resilientCacheRedisTemplate) {
        return new MultiLevelCacheAdapter(caffeineAdapter, redisAdapter, resilientCacheRedisTemplate);
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
    public CacheAspect cacheAspect(CacheEngine cacheEngine, CacheKeyGenerator cacheKeyGenerator, CacheProperties properties) {
        return new CacheAspect(cacheEngine, cacheKeyGenerator, properties);
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
    public WarmupService warmupService(CachePort activeCachePort, CacheProperties properties) {
        return new WarmupService(activeCachePort, properties);
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
            HealthScheduler healthScheduler) {
        return new CacheMonitorController(
                caffeineAdapter,
                redisAdapter,
                metricsTracker,
                eventTracker,
                cacheManager,
                properties,
                healthScheduler
        );
    }
}

```

---

## AutoConfiguration.imports (Auto-Configuration SPI Registry)

### File Path: `resilient-cache-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

### 💡 What this code does (in simple language):
This registry file tells Spring Boot 3's SPI loader to discover and execute our auto-configuration class (ResilientCacheAutoConfiguration) automatically on startup without requiring the host application to write manual imports.

### 💻 Source Code:
```properties
com.resilientcache.config.ResilientCacheAutoConfiguration

```

---

## CacheEngineException.java (Base Exception)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/exception/CacheEngineException.java`

### 💡 What this code does (in simple language):
This is an abstract base class for all custom exceptions thrown by our cache engine. It ensures every custom exception is a runtime exception, carries an engine-specific error code (e.g. CACHE_101), and logs the exact timestamp when the failure occurred.

### 💻 Source Code:
```java
package com.resilientcache.exception;

public abstract class CacheEngineException extends RuntimeException {

    private final String errorCode;
    private final long timestamp;

    protected CacheEngineException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    protected CacheEngineException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

```

---

## ProviderNotConfiguredException.java (Custom Exception)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/exception/ProviderNotConfiguredException.java`

### 💡 What this code does (in simple language):
This exception extends CacheEngineException and is thrown when the application configuration points to an invalid provider or fails to resolve/initialize the active caching bean. It is mapped to the error code 'CACHE_101' at startup.

### 💻 Source Code:
```java
package com.resilientcache.exception;

public class ProviderNotConfiguredException extends CacheEngineException {

    public ProviderNotConfiguredException(String message) {
        super(message, "CACHE_101");
    }

    public ProviderNotConfiguredException(String message, Throwable cause) {
        super(message, cause, "CACHE_101");
    }
}

```

---

## RecoveryFailedException.java (Custom Exception)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/exception/RecoveryFailedException.java`

### 💡 What this code does (in simple language):
This custom exception extends CacheEngineException and is thrown when the RecoveryManager exhausts all configured reconnection attempts (e.g., 5 retries) and fails to reconnect to Redis. Mapped to error code 'CACHE_103'.

### 💻 Source Code:
```java
package com.resilientcache.exception;

public class RecoveryFailedException extends CacheEngineException {

    public RecoveryFailedException(String message) {
        super(message, "CACHE_103");
    }

    public RecoveryFailedException(String message, Throwable cause) {
        super(message, cause, "CACHE_103");
    }
}

```

---

## ResilientCache.java (Custom Annotation)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/annotation/ResilientCache.java`

### 💡 What this code does (in simple language):
This custom annotation is placed on methods in the host application. It accepts a cache name (namespace), a Spring EL key expression (e.g., '#id'), and an optional TTL override (e.g., '5s'). When a method carries this annotation, its execution is intercepted and handled by our caching engine.

### 💻 Source Code:
```java
package com.resilientcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResilientCache {

    /**
     * Logical cache name / cache namespace.
     */
    String name() default "default";

    /**
     * SpEL expression to generate the cache key.
     * Example: "#id" or "#product.name"
     */
    String key() default "";

    /**
     * Optional TTL override in ISO-8601 duration format or string representation (e.g. "5m", "30s").
     * If left blank, default configuration TTL is applied.
     */
    String ttl() default "";
}

```

---

## CacheKeyGenerator.java (SpEL Key Evaluator)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/core/CacheKeyGenerator.java`

### 💡 What this code does (in simple language):
This utility parses the annotation's 'key' SpEL expression. It evaluates method parameter inputs at runtime (for example, reading a product object's ID argument) and compiles a unique cache key string such as 'products:100'.

### 💻 Source Code:
```java
package com.resilientcache.core;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

public class CacheKeyGenerator {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * Generates a unique cache key based on SpEL expression and method invocation arguments.
     *
     * @param cacheName     logical name of the cache
     * @param keyExpression SpEL expression (e.g. "#id")
     * @param method        the intercepted method
     * @param args          method arguments
     * @param target        the target object on which the method is invoked
     * @return a compiled cache key string
     */
    public String generateKey(String cacheName, String keyExpression, Method method, Object[] args, Object target) {
        if (keyExpression == null || keyExpression.trim().isEmpty()) {
            return cacheName + ":" + getDefaultKey(args);
        }

        EvaluationContext context = new MethodBasedEvaluationContext(target, method, args, nameDiscoverer);
        try {
            Object value = parser.parseExpression(keyExpression).getValue(context);
            return cacheName + ":" + (value != null ? value.toString() : "null");
        } catch (Exception e) {
            // Fallback to default key generation if SpEL parsing fails
            return cacheName + ":" + getDefaultKey(args);
        }
    }

    private String getDefaultKey(Object[] args) {
        if (args == null || args.length == 0) {
            return "simple";
        }
        if (args.length == 1) {
            return args[0] != null ? args[0].toString() : "null";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i] != null ? args[i].toString() : "null");
            if (i < args.length - 1) {
                sb.append("_");
            }
        }
        return sb.toString();
    }
}

```

---

## CacheAspect.java (AOP Aspect Interceptor)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/aspect/CacheAspect.java`

### 💡 What this code does (in simple language):
This class is a Spring AOP Aspect. It intercepts all method executions annotated with @ResilientCache. It extracts method metadata, delegates key generation to CacheKeyGenerator, translates simple duration strings (like '5s', '15m') into java.time.Duration objects, and triggers CacheEngine.get() to manage lookup flows.

### 💻 Source Code:
```java
package com.resilientcache.aspect;

import com.resilientcache.annotation.ResilientCache;
import com.resilientcache.config.CacheProperties;
import com.resilientcache.core.CacheEngine;
import com.resilientcache.core.CacheKeyGenerator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.time.Duration;

@Aspect
public class CacheAspect {

    private final CacheEngine cacheEngine;
    private final CacheKeyGenerator keyGenerator;
    private final CacheProperties properties;

    public CacheAspect(CacheEngine cacheEngine, CacheKeyGenerator keyGenerator, CacheProperties properties) {
        this.cacheEngine = cacheEngine;
        this.keyGenerator = keyGenerator;
        this.properties = properties;
    }

    @Around("@annotation(resilientCache)")
    public Object handleCachedMethod(ProceedingJoinPoint joinPoint, ResilientCache resilientCache) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Generate the unique cache key
        String cacheKey = keyGenerator.generateKey(
                resilientCache.name(), 
                resilientCache.key(), 
                method, 
                joinPoint.getArgs(), 
                joinPoint.getTarget()
        );

        // Resolve optional TTL override
        Duration ttlOverride = parseDuration(resilientCache.ttl());

        // Delegate to CacheEngine using the Cache-Aside pattern loader
        return cacheEngine.get(cacheKey, () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }
                throw new RuntimeException("Error executing cached method target", throwable);
            }
        }, ttlOverride);
    }

    private Duration parseDuration(String ttlStr) {
        if (ttlStr == null || ttlStr.trim().isEmpty()) {
            return null;
        }
        String s = ttlStr.trim().toLowerCase();
        try {
            if (s.endsWith("ms")) {
                return Duration.ofMillis(Long.parseLong(s.substring(0, s.length() - 2)));
            } else if (s.endsWith("s")) {
                return Duration.ofSeconds(Long.parseLong(s.substring(0, s.length() - 1)));
            } else if (s.endsWith("m")) {
                return Duration.ofMinutes(Long.parseLong(s.substring(0, s.length() - 1)));
            } else if (s.endsWith("h")) {
                return Duration.ofHours(Long.parseLong(s.substring(0, s.length() - 1)));
            } else if (s.endsWith("d")) {
                return Duration.ofDays(Long.parseLong(s.substring(0, s.length() - 1)));
            }
            return Duration.parse(ttlStr); // standard ISO-8601 fallback
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid TTL duration override: " + ttlStr, e);
        }
    }
}

```

---

## CachePort.java (Hexagonal Port Interface)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/port/CachePort.java`

### 💡 What this code does (in simple language):
This is the Hexagonal Port interface. It defines a standard contract for any cache provider adapter (reads, writes, evictions, clears, health checks). By making the core engine depend solely on this interface rather than concrete Caffeine/Redis classes, we achieve clean technology decoupling.

### 💻 Source Code:
```java
package com.resilientcache.port;

import java.time.Duration;
import java.util.Optional;

public interface CachePort {

    /**
     * Retrieve value from cache.
     *
     * @param key cache key
     * @return Optional containing the cached value, or empty if miss/expired
     */
    Optional<Object> get(String key);

    /**
     * Write value to cache.
     *
     * @param key   cache key
     * @param value cache value
     * @param ttl   Time To Live duration
     */
    void put(String key, Object value, Duration ttl);

    /**
     * Remove value from cache.
     *
     * @param key cache key
     */
    void evict(String key);

    /**
     * Clear all cached values in this provider.
     */
    void clear();

    /**
     * Health status of this provider.
     *
     * @return true if UP and responsive, false otherwise
     */
    boolean healthCheck();
}

```

---

## CacheEngine.java (Core Caching Brain)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/core/CacheEngine.java`

### 💡 What this code does (in simple language):
This is the core business orchestrator class. It coordinates the Cache-Aside pattern logic: it queries CacheManager for keys; if a cache hit is found, it logs statistics and returns the value; on a cache miss, it invokes the target service method (e.g. database query), writes the output through to the cache, logs telemetry, and returns the computed result. It records dynamic read and write latency measurements to MetricsTracker.

### 💻 Source Code:
```java
package com.resilientcache.core;

import com.resilientcache.config.CacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public class CacheEngine {

    private static final Logger log = LoggerFactory.getLogger(CacheEngine.class);

    private final CacheManager cacheManager;
    private final CacheProperties properties;
    private final com.resilientcache.monitoring.MetricsTracker metricsTracker;

    public CacheEngine(CacheManager cacheManager, CacheProperties properties, com.resilientcache.monitoring.MetricsTracker metricsTracker) {
        this.cacheManager = cacheManager;
        this.properties = properties;
        this.metricsTracker = metricsTracker;
    }

    /**
     * Retrieves a value from the cache or loads it from the origin if missing (Cache-Aside pattern).
     *
     * @param key         the cache key
     * @param valueLoader supplier to compute the value on a cache miss
     * @param ttlOverride optional TTL override
     * @return the cached or computed value
     */
    public Object get(String key, Supplier<Object> valueLoader, Duration ttlOverride) {
        long start = System.nanoTime();
        
        Optional<Object> cached = cacheManager.get(key);
        long latencyNs = System.nanoTime() - start;
        long latency = latencyNs / 1_000_000;

        if (metricsTracker != null) {
            metricsTracker.recordReadLatency(latencyNs);
        }

        // Capitalize for matching formatting (e.g. CAFFEINE, REDIS)
        String providerName = properties.getProvider().substring(0, 1).toUpperCase() 
                + properties.getProvider().substring(1).toLowerCase();

        if (cached.isPresent()) {
            log.info("Cache HIT  | Provider={} | Key={} | Latency={}ms", providerName, key, latency);
            return cached.get();
        }

        log.info("Cache MISS | Provider={} | Key={}", providerName, key);
        
        // Cache miss: load from database / origin method
        log.info("Loading from Database | Key={}", key);
        Object computed = valueLoader.get();
        
        if (computed != null) {
            Duration effectiveTtl = ttlOverride != null ? ttlOverride : properties.getDefaultTtl();
            
            long putStart = System.nanoTime();
            cacheManager.put(key, computed, effectiveTtl);
            long putLatencyNs = System.nanoTime() - putStart;
            
            if (metricsTracker != null) {
                metricsTracker.recordWriteLatency(putLatencyNs);
            }
            
            log.info("Stored in {} | TTL={}", providerName, formatDuration(effectiveTtl));
        }
        
        return computed;
    }


    public void evict(String key) {
        cacheManager.evict(key);
        log.info("Evicted key: {}", key);
    }

    public void clear() {
        cacheManager.clear();
        log.info("Cleared cache");
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "null";
        }
        long minutes = duration.toMinutes();
        if (minutes > 0) {
            return minutes + "m";
        }
        return duration.toSeconds() + "s";
    }
}

```

---

## CacheManager.java (Core Operations Delegate)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/core/CacheManager.java`

### 💡 What this code does (in simple language):
This class is a thin delegator. It acts as an intermediary, forwarding cache read, write, evict, and clear calls directly to the active CachePort adapter resolved at startup.

### 💻 Source Code:
```java
package com.resilientcache.core;

import com.resilientcache.port.CachePort;

import java.time.Duration;
import java.util.Optional;

public class CacheManager {

    private final CachePort activeCachePort;

    public CacheManager(CachePort activeCachePort) {
        this.activeCachePort = activeCachePort;
    }

    public Optional<Object> get(String key) {
        return activeCachePort.get(key);
    }

    public void put(String key, Object value, Duration ttl) {
        activeCachePort.put(key, value, ttl);
    }

    public void evict(String key) {
        activeCachePort.evict(key);
    }

    public void clear() {
        activeCachePort.clear();
    }
}

```

---

## ProviderResolver.java (Dynamic Factory)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/core/ProviderResolver.java`

### 💡 What this code does (in simple language):
This factory class receives all declared provider adapters (CaffeineAdapter, RedisAdapter, and MultiLevelCacheAdapter) and dynamically resolves which bean to use at startup based on the active provider name specified in configuration properties.

### 💻 Source Code:
```java
package com.resilientcache.core;

import com.resilientcache.exception.ProviderNotConfiguredException;
import com.resilientcache.port.CachePort;
import com.resilientcache.provider.caffeine.CaffeineAdapter;
import com.resilientcache.provider.multi.MultiLevelCacheAdapter;
import com.resilientcache.provider.redis.RedisAdapter;

public class ProviderResolver {

    private final CaffeineAdapter caffeineAdapter;
    private final RedisAdapter redisAdapter;
    private final MultiLevelCacheAdapter multiLevelCacheAdapter;

    public ProviderResolver(CaffeineAdapter caffeineAdapter, RedisAdapter redisAdapter, MultiLevelCacheAdapter multiLevelCacheAdapter) {
        this.caffeineAdapter = caffeineAdapter;
        this.redisAdapter = redisAdapter;
        this.multiLevelCacheAdapter = multiLevelCacheAdapter;
    }

    /**
     * Resolves the active CachePort implementation based on the configured provider name.
     *
     * @param providerName configured provider name (caffeine, redis, multi-level)
     * @return the matching active CachePort bean
     * @throws ProviderNotConfiguredException if the provider name is unknown or if a requested adapter is null
     */
    public CachePort resolve(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new ProviderNotConfiguredException("No cache provider configured. Please set 'resilient-cache.provider'");
        }

        String target = providerName.trim().toLowerCase();
        switch (target) {
            case "caffeine":
                if (caffeineAdapter == null) {
                    throw new ProviderNotConfiguredException("Caffeine provider was selected but is not initialized.");
                }
                return caffeineAdapter;
            case "redis":
                if (redisAdapter == null) {
                    throw new ProviderNotConfiguredException("Redis provider was selected but is not initialized.");
                }
                return redisAdapter;
            case "multi-level":
                if (multiLevelCacheAdapter == null) {
                    throw new ProviderNotConfiguredException("Multi-level provider was selected but is not initialized.");
                }
                return multiLevelCacheAdapter;
            default:
                throw new ProviderNotConfiguredException("Unsupported cache provider value: '" + providerName 
                        + "'. Supported values are: caffeine, redis, multi-level");
        }
    }
}

```

---

## CaffeineAdapter.java (Caffeine L1 Cache Adapter)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/provider/caffeine/CaffeineAdapter.java`

### 💡 What this code does (in simple language):
This class implements CachePort to connect our engine to the Caffeine in-process memory cache. It employs a custom Expiry policy that wraps cached values in a CacheValue container, allowing the cache to enforce distinct, customized TTL durations for individual entries. It also enables recordStats() to retrieve native Caffeine hit/miss statistics.

### 💻 Source Code:
```java
package com.resilientcache.provider.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.resilientcache.config.CacheProperties;
import com.resilientcache.port.CachePort;

import java.time.Duration;
import java.util.Optional;

public class CaffeineAdapter implements CachePort {

    private final Cache<String, CacheValue> cache;
    private final CacheProperties properties;

    public CaffeineAdapter(CacheProperties properties) {
        this.properties = properties;
        this.cache = Caffeine.newBuilder()
                .maximumSize(properties.getCaffeine().getMaximumSize())
                .recordStats()
                .expireAfter(new Expiry<String, CacheValue>() {
                    @Override
                    public long expireAfterCreate(String key, CacheValue value, long currentTime) {
                        return value.getTtl().toNanos();
                    }

                    @Override
                    public long expireAfterUpdate(String key, CacheValue value, long currentTime, long currentDuration) {
                        return value.getTtl().toNanos();
                    }

                    @Override
                    public long expireAfterRead(String key, CacheValue value, long currentTime, long currentDuration) {
                        return currentDuration; // Keep current remaining TTL on read
                    }
                })
                .build();
    }

    public com.github.benmanes.caffeine.cache.stats.CacheStats getStats() {
        return cache.stats();
    }


    @Override
    public Optional<Object> get(String key) {
        CacheValue cacheValue = cache.getIfPresent(key);
        if (cacheValue == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cacheValue.getValue());
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        if (value == null) {
            return;
        }
        Duration effectiveTtl = ttl != null ? ttl : properties.getCaffeine().getExpireAfterWrite();
        cache.put(key, new CacheValue(value, effectiveTtl));
    }

    @Override
    public void evict(String key) {
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    public boolean healthCheck() {
        // Local in-memory cache is always healthy unless JVM itself is out of memory
        return true;
    }

    /**
     * Wrapper class to store cache value along with its specific TTL duration.
     */
    private static class CacheValue {
        private final Object value;
        private final Duration ttl;

        public CacheValue(Object value, Duration ttl) {
            this.value = value;
            this.ttl = ttl;
        }

        public Object getValue() {
            return value;
        }

        public Duration getTtl() {
            return ttl;
        }
    }
}

```

---

## RedisAdapter.java (Redis L2 Cache Adapter)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/provider/redis/RedisAdapter.java`

### 💡 What this code does (in simple language):
This class implements CachePort to connect our engine to the Redis server using RedisTemplate. It writes objects serialized to JSON, manages remote keys, and handles health monitoring by pinging the Lettuce connection channel. It records L2 hit and miss statistics to MetricsTracker.

### 💻 Source Code:
```java
package com.resilientcache.provider.redis;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.port.CachePort;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public class RedisAdapter implements CachePort {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties properties;
    private final com.resilientcache.monitoring.MetricsTracker metricsTracker;

    public RedisAdapter(RedisTemplate<String, Object> redisTemplate, CacheProperties properties, com.resilientcache.monitoring.MetricsTracker metricsTracker) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.metricsTracker = metricsTracker;
    }

    @Override
    public Optional<Object> get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (metricsTracker != null) {
                if (value != null) {
                    metricsTracker.incrementL2Hit();
                } else {
                    metricsTracker.incrementL2Miss();
                }
            }
            return Optional.ofNullable(value);
        } catch (Exception e) {
            if (metricsTracker != null) {
                metricsTracker.incrementL2Miss();
            }
            // Log warning or degrade gracefully
            return Optional.empty();
        }
    }


    @Override
    public void put(String key, Object value, Duration ttl) {
        if (value == null) {
            return;
        }
        try {
            Duration effectiveTtl = ttl != null ? ttl : properties.getDefaultTtl();
            redisTemplate.opsForValue().set(key, value, effectiveTtl);
        } catch (Exception e) {
            // Log warning or degrade gracefully
        }
    }

    @Override
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            // Log warning
        }
    }

    @Override
    public void clear() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            // Log warning
        }
    }

    @Override
    public boolean healthCheck() {
        if (redisTemplate.getConnectionFactory() == null) {
            return false;
        }
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            String pingResult = connection.ping();
            return "PONG".equalsIgnoreCase(pingResult);
        } catch (Exception e) {
            return false;
        }
    }
}

```

---

## MultiLevelCacheAdapter.java (Two-Tier L1/L2 Orchestrator)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/provider/multi/MultiLevelCacheAdapter.java`

### 💡 What this code does (in simple language):
This is the two-tier caching orchestrator implementing CachePort. It coordinates Caffeine (L1) and Redis (L2): on lookup, it checks L1; if L1 misses, it checks L2; if L2 hits, it reads Redis's remaining TTL and back-propagates the value into L1 with matching duration to maintain consistency; on writes, it writes through both levels simultaneously.

### 💻 Source Code:
```java
package com.resilientcache.provider.multi;

import com.resilientcache.port.CachePort;
import com.resilientcache.provider.caffeine.CaffeineAdapter;
import com.resilientcache.provider.redis.RedisAdapter;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MultiLevelCacheAdapter implements CachePort {

    private final CaffeineAdapter caffeineAdapter;
    private final RedisAdapter redisAdapter;
    private final RedisTemplate<String, Object> redisTemplate;

    public MultiLevelCacheAdapter(CaffeineAdapter caffeineAdapter, RedisAdapter redisAdapter, RedisTemplate<String, Object> redisTemplate) {
        this.caffeineAdapter = caffeineAdapter;
        this.redisAdapter = redisAdapter;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<Object> get(String key) {
        // Step 1: Query L1 (Caffeine)
        Optional<Object> l1Value = caffeineAdapter.get(key);
        if (l1Value.isPresent()) {
            return l1Value;
        }

        // Step 2: Query L2 (Redis) on L1 miss
        Optional<Object> l2Value = redisAdapter.get(key);
        if (l2Value.isPresent()) {
            Object value = l2Value.get();
            // Step 3: Back-propagate to L1 with remaining Redis TTL
            try {
                Long remainingTtlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (remainingTtlSeconds != null && remainingTtlSeconds > 0) {
                    caffeineAdapter.put(key, value, Duration.ofSeconds(remainingTtlSeconds));
                } else {
                    caffeineAdapter.put(key, value, null); // Fallback to default Caffeine TTL
                }
            } catch (Exception e) {
                // Degrade gracefully: still return the value even if back-propagation fails
                caffeineAdapter.put(key, value, null);
            }
            return Optional.of(value);
        }

        return Optional.empty();
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        // Write-through to both cache levels
        caffeineAdapter.put(key, value, ttl);
        redisAdapter.put(key, value, ttl);
    }

    @Override
    public void evict(String key) {
        caffeineAdapter.evict(key);
        redisAdapter.evict(key);
    }

    @Override
    public void clear() {
        caffeineAdapter.clear();
        redisAdapter.clear();
    }

    @Override
    public boolean healthCheck() {
        // Multi-level is considered fully healthy if both tiers are healthy
        return caffeineAdapter.healthCheck() && redisAdapter.healthCheck();
    }
}

```

---

## HealthStatus.java (Health Status Enum)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/event/HealthStatus.java`

### 💡 What this code does (in simple language):
An enum representing the possible states of a cache provider: UP (fully operational), DOWN (unresponsive or disconnected), or DEGRADED (operating with failures).

### 💻 Source Code:
```java
package com.resilientcache.event;

public enum HealthStatus {
    UP,
    DOWN,
    DEGRADED
}

```

---

## HealthStatusChangedEvent.java (Spring Event)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/event/HealthStatusChangedEvent.java`

### 💡 What this code does (in simple language):
Spring event published by the HealthScheduler when a cache provider's health state changes (for instance, transitioning from UP to DOWN or DOWN to UP).

### 💻 Source Code:
```java
package com.resilientcache.event;

public class HealthStatusChangedEvent {

    private final String providerName;
    private final HealthStatus previousStatus;
    private final HealthStatus newStatus;
    private final long timestamp;

    public HealthStatusChangedEvent(String providerName, HealthStatus previousStatus, HealthStatus newStatus) {
        this.providerName = providerName;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.timestamp = System.currentTimeMillis();
    }

    public String getProviderName() {
        return providerName;
    }

    public HealthStatus getPreviousStatus() {
        return previousStatus;
    }

    public HealthStatus getNewStatus() {
        return newStatus;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

```

---

## ProviderFailureEvent.java (Spring Event)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/event/ProviderFailureEvent.java`

### 💡 What this code does (in simple language):
Spring event published when a provider failure is verified. This event is intercepted by the RecoveryManager to trigger automated self-healing retry cycles.

### 💻 Source Code:
```java
package com.resilientcache.event;

public class ProviderFailureEvent {

    private final String providerName;
    private final String reason;
    private final long timestamp;

    public ProviderFailureEvent(String providerName, String reason) {
        this.providerName = providerName;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
    }

    public String getProviderName() {
        return providerName;
    }

    public String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

```

---

## RecoveryStartedEvent.java (Spring Event)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/event/RecoveryStartedEvent.java`

### 💡 What this code does (in simple language):
Spring event published immediately when the RecoveryManager begins attempting reconnection retries for a failed cache provider.

### 💻 Source Code:
```java
package com.resilientcache.event;

public class RecoveryStartedEvent {

    private final String providerName;
    private final long timestamp;

    public RecoveryStartedEvent(String providerName) {
        this.providerName = providerName;
        this.timestamp = System.currentTimeMillis();
    }

    public String getProviderName() {
        return providerName;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

```

---

## RecoveryCompletedEvent.java (Spring Event)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/event/RecoveryCompletedEvent.java`

### 💡 What this code does (in simple language):
Spring event published when the RecoveryManager successfully restores connection to the cache provider. Holds metrics like the total retry count and recovery duration.

### 💻 Source Code:
```java
package com.resilientcache.event;

public class RecoveryCompletedEvent {

    private final String providerName;
    private final int retryAttempts;
    private final long durationMs;
    private final long timestamp;

    public RecoveryCompletedEvent(String providerName, int retryAttempts, long durationMs) {
        this.providerName = providerName;
        this.retryAttempts = retryAttempts;
        this.durationMs = durationMs;
        this.timestamp = System.currentTimeMillis();
    }

    public String getProviderName() {
        return providerName;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

```

---

## HealthScheduler.java (Health Checker Scheduler)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/monitoring/HealthScheduler.java`

### 💡 What this code does (in simple language):
Runs a background scheduled task every 5 seconds. It pings the active cache provider; on status transitions (like UP -> DOWN), it broadcasts event notifications and alerts the RecoveryManager. Uses a numeric default to avoid scheduler parsing errors.

### 💻 Source Code:
```java
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

```

---

## RecoveryManager.java (Self-Healing Recovery Manager)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/recovery/RecoveryManager.java`

### 💡 What this code does (in simple language):
A listener that executes asynchronously. When a ProviderFailureEvent is received, it runs a reconnection loop backed by Resilience4j with exponential backoff (retrying 3 times by default with exponential delays) to verify recovery, logging warning alerts for each attempt.

### 💻 Source Code:
```java
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

```

---

## WarmupService.java (Cache Key Preloading Service)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/warmup/WarmupService.java`

### 💡 What this code does (in simple language):
Listens asynchronously to RecoveryCompletedEvent. Upon successful cache restoration, it preloads a defined list of hot keys (simulated with product IDs 100/200 in M3) back into the cache to prevent load spikes on backend databases.

### 💻 Source Code:
```java
package com.resilientcache.warmup;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.event.RecoveryCompletedEvent;
import com.resilientcache.port.CachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

public class WarmupService {

    private static final Logger log = LoggerFactory.getLogger(WarmupService.class);

    private final CachePort activeCachePort;
    private final CacheProperties properties;

    public WarmupService(CachePort activeCachePort, CacheProperties properties) {
        this.activeCachePort = activeCachePort;
        this.properties = properties;
    }

    /**
     * Listens asynchronously to recovery success and triggers preloading of hot keys.
     */
    @EventListener
    @Async("resilientCacheTaskExecutor")
    public void onRecoveryCompleted(RecoveryCompletedEvent event) {
        if (!properties.getWarmup().isEnabled()) {
            return;
        }

        log.info("Warm-up Started");

        int keysLoaded = 0;
        try {
            // Mock preloading hot keys for Milestone 3 (will hook into PostgreSQL persistence in Milestone 5)
            String key1 = "products:100";
            String val1 = "ProductData[ID=100, Name=Premium Widget 100, Price=$99.99]";
            activeCachePort.put(key1, val1, properties.getDefaultTtl());
            keysLoaded++;

            String key2 = "products:200";
            String val2 = "ProductDataShortTTL[ID=200, Name=Transient Widget 200]";
            // Use Caffeine default or Redis default
            activeCachePort.put(key2, val2, properties.getDefaultTtl());
            keysLoaded++;

            log.info("Warm-up Completed | Loaded={} Keys", keysLoaded);

        } catch (Exception e) {
            log.error("Warm-up Failed during preloading: {}", e.getMessage());
        }
    }
}

```

---

## MetricsTracker.java (Cache Metrics Accumulator)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/monitoring/MetricsTracker.java`

### 💡 What this code does (in simple language):
Thread-safe monitor that records read/write latencies (in nanoseconds) and counts L2 cache hits and misses. Integrates Micrometer MeterRegistry to export metrics data directly to Actuator.

### 💻 Source Code:
```java
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

```

---

## EventTracker.java (Cache Resilience Event Tracker)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/monitoring/EventTracker.java`

### 💡 What this code does (in simple language):
Event listener that tracks and logs cache health status changes, outages, recovery processes, and manual clears, keeping a thread-safe circular log of the last 50 events.

### 💻 Source Code:
```java
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

```

---

## CacheMonitorController.java (REST Controller Endpoint)

### File Path: `resilient-cache-starter/src/main/java/com/resilientcache/controller/CacheMonitorController.java`

### 💡 What this code does (in simple language):
REST API Controller exposing JSON endpoints for stats, health, and event lists, and handling POST requests to clear/evict all caches.

### 💻 Source Code:
```java
package com.resilientcache.controller;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.core.CacheManager;
import com.resilientcache.event.HealthStatus;
import com.resilientcache.monitoring.EventTracker;
import com.resilientcache.monitoring.HealthScheduler;
import com.resilientcache.monitoring.MetricsTracker;
import com.resilientcache.provider.caffeine.CaffeineAdapter;
import com.resilientcache.provider.redis.RedisAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheMonitorController {

    private final CaffeineAdapter caffeineAdapter;
    private final RedisAdapter redisAdapter;
    private final MetricsTracker metricsTracker;
    private final EventTracker eventTracker;
    private final CacheManager cacheManager;
    private final CacheProperties properties;
    private final HealthScheduler healthScheduler;

    public CacheMonitorController(
            CaffeineAdapter caffeineAdapter,
            @Autowired(required = false) RedisAdapter redisAdapter,
            MetricsTracker metricsTracker,
            EventTracker eventTracker,
            CacheManager cacheManager,
            CacheProperties properties,
            HealthScheduler healthScheduler) {
        this.caffeineAdapter = caffeineAdapter;
        this.redisAdapter = redisAdapter;
        this.metricsTracker = metricsTracker;
        this.eventTracker = eventTracker;
        this.cacheManager = cacheManager;
        this.properties = properties;
        this.healthScheduler = healthScheduler;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // L1 Caffeine Stats
        com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats = caffeineAdapter.getStats();
        long l1Hits = caffeineStats.hitCount();
        long l1Misses = caffeineStats.missCount();
        double l1HitRatio = caffeineStats.hitRate();

        // L2 Redis Stats
        long l2Hits = metricsTracker.getL2HitCount();
        long l2Misses = metricsTracker.getL2MissCount();
        long totalL2Queries = l2Hits + l2Misses;
        double l2HitRatio = totalL2Queries == 0 ? 0.0 : (double) l2Hits / totalL2Queries;

        stats.put("provider", properties.getProvider());
        stats.put("l1Hits", l1Hits);
        stats.put("l1Misses", l1Misses);
        stats.put("l1HitRatio", l1HitRatio);
        
        stats.put("l2Hits", l2Hits);
        stats.put("l2Misses", l2Misses);
        stats.put("l2HitRatio", l2HitRatio);

        stats.put("averageReadLatencyMs", metricsTracker.getAverageReadLatencyMs());
        stats.put("averageWriteLatencyMs", metricsTracker.getAverageWriteLatencyMs());
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        
        HealthStatus engineStatus = healthScheduler.getCurrentStatus();
        health.put("status", engineStatus.name());
        health.put("l1Status", "UP"); // Caffeine is local in-memory, always UP
        
        boolean l2Healthy = false;
        if (redisAdapter != null) {
            try {
                l2Healthy = redisAdapter.healthCheck();
            } catch (Exception e) {
                // Ignore and keep false
            }
        }
        health.put("l2Status", l2Healthy ? "UP" : "DOWN");
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/events")
    public ResponseEntity<List<Map<String, Object>>> getEvents() {
        return ResponseEntity.ok(eventTracker.getEvents());
    }

    @PostMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCache() {
        cacheManager.clear();
        
        eventTracker.addManualEvent(
            "CACHE_CLEAR", 
            properties.getProvider(), 
            "Cache cleared manually by admin command"
        );
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cache cleared successfully");
        return ResponseEntity.ok(response);
    }
}

```

---

## cache-dashboard.html (Web Dashboard Interface)

### File Path: `resilient-cache-starter/src/main/resources/static/cache-dashboard.html`

### 💡 What this code does (in simple language):
A premium dark-themed HTML/CSS/JS dashboard served as a static resource. Visualizes cache topology, L1/L2 hits, misses, ratios, read/write latencies, event logs, and features a one-click cache flush operation.

### 💻 Source Code:
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Resilient Cache Engine Dashboard</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-color: #0b0f19;
            --card-bg: rgba(17, 24, 39, 0.7);
            --card-border: rgba(255, 255, 255, 0.08);
            --text-primary: #f3f4f6;
            --text-secondary: #9ca3af;
            --primary: #3b82f6;
            --success: #10b981;
            --warning: #f59e0b;
            --danger: #ef4444;
            --glow-success: rgba(16, 185, 129, 0.15);
            --glow-danger: rgba(239, 68, 68, 0.15);
            --glow-primary: rgba(59, 130, 246, 0.15);
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Outfit', sans-serif;
        }

        body {
            background-color: var(--bg-color);
            background-image: 
                radial-gradient(at 10% 20%, rgba(59, 130, 246, 0.1) 0px, transparent 50%),
                radial-gradient(at 90% 80%, rgba(16, 185, 129, 0.08) 0px, transparent 50%);
            color: var(--text-primary);
            min-height: 100vh;
            padding: 2rem;
            overflow-x: hidden;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
        }

        /* Header Style */
        header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2.5rem;
            border-bottom: 1px solid var(--card-border);
            padding-bottom: 1.5rem;
        }

        .brand-section {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .brand-logo {
            width: 45px;
            height: 45px;
            border-radius: 12px;
            background: linear-gradient(135deg, var(--primary), var(--success));
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 1.5rem;
            color: #fff;
            box-shadow: 0 0 20px rgba(59, 130, 246, 0.4);
        }

        h1 {
            font-size: 1.8rem;
            font-weight: 600;
            background: linear-gradient(to right, #fff, var(--text-secondary));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .engine-status-badge {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            background: rgba(16, 185, 129, 0.1);
            border: 1px solid rgba(16, 185, 129, 0.2);
            padding: 0.5rem 1rem;
            border-radius: 9999px;
            font-size: 0.85rem;
            font-weight: 500;
            color: var(--success);
            box-shadow: 0 0 15px var(--glow-success);
            transition: all 0.3s ease;
        }

        .pulse-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background-color: var(--success);
            animation: pulse 1.8s infinite;
        }

        @keyframes pulse {
            0% { transform: scale(0.9); opacity: 0.6; }
            50% { transform: scale(1.3); opacity: 1; box-shadow: 0 0 10px var(--success); }
            100% { transform: scale(0.9); opacity: 0.6; }
        }

        /* Dashboard Grid Layout */
        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(12, 1fr);
            gap: 1.5rem;
            margin-bottom: 2rem;
        }

        .card {
            background: var(--card-bg);
            border: 1px solid var(--card-border);
            border-radius: 16px;
            padding: 1.5rem;
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
            transition: transform 0.3s ease, border-color 0.3s ease;
        }

        .card:hover {
            border-color: rgba(255, 255, 255, 0.15);
            transform: translateY(-2px);
        }

        /* Span Helpers */
        .col-4 { grid-column: span 4; }
        .col-6 { grid-column: span 6; }
        .col-8 { grid-column: span 8; }
        .col-12 { grid-column: span 12; }

        @media (max-width: 900px) {
            .col-4, .col-6, .col-8 { grid-column: span 12; }
        }

        /* Card Typography */
        .card-title {
            font-size: 1.05rem;
            font-weight: 600;
            color: var(--text-secondary);
            margin-bottom: 1.25rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .stat-value {
            font-size: 2.2rem;
            font-weight: 700;
            color: #fff;
            margin-bottom: 0.5rem;
        }

        .stat-desc {
            font-size: 0.85rem;
            color: var(--text-secondary);
        }

        /* Health Indicators styling */
        .health-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.75rem 0;
            border-bottom: 1px solid rgba(255, 255, 255, 0.05);
        }

        .health-row:last-child {
            border-bottom: none;
            padding-bottom: 0;
        }

        .tier-label {
            font-size: 1rem;
            font-weight: 500;
            color: var(--text-primary);
        }

        .tier-status {
            display: flex;
            align-items: center;
            gap: 0.4rem;
            font-size: 0.85rem;
            font-weight: 600;
            padding: 0.25rem 0.75rem;
            border-radius: 6px;
        }

        .status-up {
            background-color: rgba(16, 185, 129, 0.1);
            color: var(--success);
            border: 1px solid rgba(16, 185, 129, 0.2);
        }

        .status-down {
            background-color: rgba(239, 68, 68, 0.1);
            color: var(--danger);
            border: 1px solid rgba(239, 68, 68, 0.2);
        }

        /* Hit Ratio Rings/Bars */
        .progress-bar-container {
            margin-top: 1rem;
        }

        .progress-label {
            display: flex;
            justify-content: space-between;
            font-size: 0.9rem;
            margin-bottom: 0.4rem;
        }

        .progress-track {
            width: 100%;
            height: 10px;
            background-color: rgba(255, 255, 255, 0.05);
            border-radius: 9999px;
            overflow: hidden;
        }

        .progress-fill {
            height: 100%;
            border-radius: 9999px;
            width: 0%;
            transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .fill-l1 {
            background: linear-gradient(90deg, var(--primary), #60a5fa);
            box-shadow: 0 0 10px rgba(59, 130, 246, 0.3);
        }

        .fill-l2 {
            background: linear-gradient(90deg, var(--success), #34d399);
            box-shadow: 0 0 10px rgba(16, 185, 129, 0.3);
        }

        /* Latency Gauge details */
        .latency-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1rem;
        }

        .latency-subcard {
            background: rgba(255, 255, 255, 0.02);
            border: 1px solid rgba(255, 255, 255, 0.04);
            border-radius: 12px;
            padding: 1rem;
            text-align: center;
        }

        .latency-val {
            font-size: 1.6rem;
            font-weight: 700;
            color: #fff;
            margin-bottom: 0.25rem;
        }

        /* Actions Section */
        .action-button {
            width: 100%;
            background: linear-gradient(135deg, var(--primary), #2563eb);
            border: none;
            color: white;
            padding: 0.85rem;
            border-radius: 10px;
            font-size: 0.95rem;
            font-weight: 600;
            cursor: pointer;
            box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
            transition: all 0.2s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
        }

        .action-button:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
        }

        .action-button:active {
            transform: translateY(1px);
        }

        .action-button.clear-btn {
            background: linear-gradient(135deg, var(--danger), #dc2626);
            box-shadow: 0 4px 15px rgba(239, 68, 68, 0.3);
        }

        .action-button.clear-btn:hover {
            box-shadow: 0 6px 20px rgba(239, 68, 68, 0.4);
        }

        /* Event Timeline styling */
        .timeline-container {
            max-height: 250px;
            overflow-y: auto;
            padding-right: 0.5rem;
        }

        .timeline-container::-webkit-scrollbar {
            width: 6px;
        }

        .timeline-container::-webkit-scrollbar-track {
            background: transparent;
        }

        .timeline-container::-webkit-scrollbar-thumb {
            background: rgba(255, 255, 255, 0.1);
            border-radius: 10px;
        }

        .timeline-item {
            display: flex;
            gap: 1rem;
            padding: 0.85rem 0;
            border-left: 2px solid rgba(255, 255, 255, 0.05);
            padding-left: 1.25rem;
            position: relative;
        }

        .timeline-item:last-child {
            padding-bottom: 0;
        }

        .timeline-dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            position: absolute;
            left: -6px;
            top: 1.1rem;
            border: 2px solid var(--bg-color);
        }

        .dot-HEALTH_CHANGE { background-color: var(--warning); box-shadow: 0 0 8px var(--warning); }
        .dot-FAILURE { background-color: var(--danger); box-shadow: 0 0 8px var(--danger); }
        .dot-RECOVERY_START { background-color: var(--primary); box-shadow: 0 0 8px var(--primary); }
        .dot-RECOVERY_SUCCESS { background-color: var(--success); box-shadow: 0 0 8px var(--success); }
        .dot-CACHE_CLEAR { background-color: #a855f7; box-shadow: 0 0 8px #a855f7; }

        .timeline-content {
            display: flex;
            flex-direction: column;
            gap: 0.2rem;
            width: 100%;
        }

        .timeline-header {
            display: flex;
            justify-content: space-between;
            font-size: 0.8rem;
            color: var(--text-secondary);
        }

        .timeline-msg {
            font-size: 0.9rem;
            color: var(--text-primary);
            line-height: 1.4;
        }

        .timeline-badge {
            font-size: 0.7rem;
            font-weight: 700;
            padding: 0.1rem 0.4rem;
            border-radius: 4px;
            text-transform: uppercase;
        }

        .badge-HEALTH_CHANGE { background-color: rgba(245, 158, 11, 0.1); color: var(--warning); }
        .badge-FAILURE { background-color: rgba(239, 68, 68, 0.1); color: var(--danger); }
        .badge-RECOVERY_START { background-color: rgba(59, 130, 246, 0.1); color: var(--primary); }
        .badge-RECOVERY_SUCCESS { background-color: rgba(16, 185, 129, 0.1); color: var(--success); }
        .badge-CACHE_CLEAR { background-color: rgba(168, 85, 247, 0.1); color: #a855f7; }

        .empty-timeline {
            text-align: center;
            color: var(--text-secondary);
            padding: 2rem 0;
            font-size: 0.95rem;
        }

        /* Banner Notification alert */
        .toast-notification {
            position: fixed;
            bottom: 2rem;
            right: 2rem;
            background: rgba(17, 24, 39, 0.9);
            border: 1px solid var(--success);
            padding: 1rem 1.5rem;
            border-radius: 10px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
            display: flex;
            align-items: center;
            gap: 0.75rem;
            transform: translateY(150%);
            transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            z-index: 9999;
            backdrop-filter: blur(10px);
        }

        .toast-active {
            transform: translateY(0);
        }

        .toast-icon {
            width: 20px;
            height: 20px;
            border-radius: 50%;
            background-color: var(--success);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: bold;
            font-size: 0.8rem;
        }
    </style>
</head>
<body>

<div class="container">
    <header>
        <div class="brand-section">
            <div class="brand-logo">R</div>
            <div>
                <h1>Resilient Cache</h1>
                <p style="font-size: 0.8rem; color: var(--text-secondary);">Self-Healing Monitoring Console</p>
            </div>
        </div>
        <div class="engine-status-badge" id="engine-health">
            <div class="pulse-dot" id="engine-health-dot"></div>
            <span id="engine-health-text">ENGINE ACTIVE</span>
        </div>
    </header>

    <div class="dashboard-grid">
        <!-- Topology Card -->
        <div class="card col-4">
            <div class="card-title">Caching Topology</div>
            <div class="stat-value" id="active-provider" style="font-size: 1.8rem; text-transform: capitalize; margin-bottom: 1.25rem;">Multi-level</div>
            
            <div class="health-row">
                <span class="tier-label">L1 local Cache (Caffeine)</span>
                <span class="tier-status status-up" id="l1-status">
                    <span class="pulse-dot" style="width:6px; height:6px;"></span>UP
                </span>
            </div>
            
            <div class="health-row">
                <span class="tier-label">L2 Shared Cache (Redis)</span>
                <span class="tier-status status-up" id="l2-status">
                    <span class="pulse-dot" style="width:6px; height:6px;" id="l2-status-dot"></span><span id="l2-status-text">UP</span>
                </span>
            </div>
        </div>

        <!-- Latency Metrics -->
        <div class="card col-4">
            <div class="card-title">Average Latencies</div>
            <div class="latency-grid">
                <div class="latency-subcard">
                    <div class="latency-val" id="read-latency">0.0ms</div>
                    <div class="stat-desc">Read Duration</div>
                </div>
                <div class="latency-subcard">
                    <div class="latency-val" id="write-latency">0.0ms</div>
                    <div class="stat-desc">Write Duration</div>
                </div>
            </div>
        </div>

        <!-- Operations Console -->
        <div class="card col-4" style="display: flex; flex-direction: column; justify-content: space-between;">
            <div>
                <div class="card-title">Operations Console</div>
                <p style="font-size: 0.85rem; color: var(--text-secondary); line-height: 1.4; margin-bottom: 1.25rem;">
                    Perform administrative operations on the active cache. Flushing clears all database keys immediately.
                </p>
            </div>
            <button class="action-button clear-btn" onclick="flushCache()">
                <svg width="16" height="16" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24"><path d="M3 6h18m-2 0v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6m3 0V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path></svg>
                Flush All Caches
            </button>
        </div>

        <!-- Statistics & Ratios -->
        <div class="card col-6">
            <div class="card-title">L1 Caffeine Statistics</div>
            <div class="progress-bar-container">
                <div class="progress-label">
                    <span>Cache Hit Ratio</span>
                    <span id="l1-hit-ratio-text">0%</span>
                </div>
                <div class="progress-track">
                    <div class="progress-fill fill-l1" id="l1-hit-ratio-fill"></div>
                </div>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 1.5rem;">
                <div>
                    <div class="stat-value" id="l1-hits" style="font-size: 1.6rem; margin-bottom: 0;">0</div>
                    <div class="stat-desc">Total Hits</div>
                </div>
                <div>
                    <div class="stat-value" id="l1-misses" style="font-size: 1.6rem; margin-bottom: 0;">0</div>
                    <div class="stat-desc">Total Misses</div>
                </div>
            </div>
        </div>

        <div class="card col-6">
            <div class="card-title">L2 Redis Statistics</div>
            <div class="progress-bar-container">
                <div class="progress-label">
                    <span>Cache Hit Ratio</span>
                    <span id="l2-hit-ratio-text">0%</span>
                </div>
                <div class="progress-track">
                    <div class="progress-fill fill-l2" id="l2-hit-ratio-fill"></div>
                </div>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 1.5rem;">
                <div>
                    <div class="stat-value" id="l2-hits" style="font-size: 1.6rem; margin-bottom: 0;">0</div>
                    <div class="stat-desc">Total Hits</div>
                </div>
                <div>
                    <div class="stat-value" id="l2-misses" style="font-size: 1.6rem; margin-bottom: 0;">0</div>
                    <div class="stat-desc">Total Misses</div>
                </div>
            </div>
        </div>

        <!-- Event Logs -->
        <div class="card col-12">
            <div class="card-title">Resilience & Event History Timeline</div>
            <div class="timeline-container" id="events-timeline">
                <div class="empty-timeline">Loading event history...</div>
            </div>
        </div>
    </div>
</div>

<!-- Toast Alert -->
<div class="toast-notification" id="toast-alert">
    <div class="toast-icon">✓</div>
    <span id="toast-message">Cache cleared successfully!</span>
</div>

<script>
    // Poll endpoints in real-time
    function fetchHealth() {
        fetch('/api/cache/health')
            .then(res => res.json())
            .then(data => {
                // Update active health status
                const healthDot = document.getElementById('engine-health-dot');
                const healthText = document.getElementById('engine-health-text');
                const healthBadge = document.getElementById('engine-health');
                
                if (data.status === 'UP') {
                    healthText.textContent = 'ENGINE ACTIVE';
                    healthDot.style.backgroundColor = 'var(--success)';
                    healthBadge.style.color = 'var(--success)';
                    healthBadge.style.borderColor = 'rgba(16, 185, 129, 0.2)';
                    healthBadge.style.boxShadow = '0 0 15px var(--glow-success)';
                } else if (data.status === 'DOWN') {
                    healthText.textContent = 'OUTAGE DETECTED';
                    healthDot.style.backgroundColor = 'var(--danger)';
                    healthBadge.style.color = 'var(--danger)';
                    healthBadge.style.borderColor = 'rgba(239, 68, 68, 0.2)';
                    healthBadge.style.boxShadow = '0 0 15px var(--glow-danger)';
                } else {
                    healthText.textContent = 'ENGINE DEGRADED';
                    healthDot.style.backgroundColor = 'var(--warning)';
                    healthBadge.style.color = 'var(--warning)';
                    healthBadge.style.borderColor = 'rgba(245, 158, 11, 0.2)';
                    healthBadge.style.boxShadow = '0 0 15px rgba(245, 158, 11, 0.15)';
                }

                // Update individual status cells
                updateStatusCell('l1-status', data.l1Status);
                updateStatusCell('l2-status', data.l2Status, 'l2-status-dot', 'l2-status-text');
            })
            .catch(err => console.error("Error fetching health:", err));
    }

    function updateStatusCell(elementId, statusVal, dotId = null, textId = null) {
        const cell = document.getElementById(elementId);
        if (!cell) return;

        if (statusVal === 'UP') {
            cell.className = 'tier-status status-up';
            if (dotId) document.getElementById(dotId).style.backgroundColor = 'var(--success)';
            if (textId) document.getElementById(textId).textContent = 'UP';
        } else {
            cell.className = 'tier-status status-down';
            if (dotId) document.getElementById(dotId).style.backgroundColor = 'var(--danger)';
            if (textId) document.getElementById(textId).textContent = 'DOWN';
        }
    }

    function fetchStats() {
        fetch('/api/cache/stats')
            .then(res => res.json())
            .then(data => {
                document.getElementById('active-provider').textContent = data.provider;
                
                // Read & Write Latency
                document.getElementById('read-latency').textContent = data.averageReadLatencyMs.toFixed(2) + 'ms';
                document.getElementById('write-latency').textContent = data.averageWriteLatencyMs.toFixed(2) + 'ms';

                // L1 Caffeine details
                document.getElementById('l1-hits').textContent = data.l1Hits;
                document.getElementById('l1-misses').textContent = data.l1Misses;
                const l1RatioPct = Math.round(data.l1HitRatio * 100);
                document.getElementById('l1-hit-ratio-text').textContent = l1RatioPct + '%';
                document.getElementById('l1-hit-ratio-fill').style.width = l1RatioPct + '%';

                // L2 Redis details
                document.getElementById('l2-hits').textContent = data.l2Hits;
                document.getElementById('l2-misses').textContent = data.l2Misses;
                const l2RatioPct = Math.round(data.l2HitRatio * 100);
                document.getElementById('l2-hit-ratio-text').textContent = l2RatioPct + '%';
                document.getElementById('l2-hit-ratio-fill').style.width = l2RatioPct + '%';
            })
            .catch(err => console.error("Error fetching stats:", err));
    }

    function fetchEvents() {
        fetch('/api/cache/events')
            .then(res => res.json())
            .then(data => {
                const timeline = document.getElementById('events-timeline');
                if (data.length === 0) {
                    timeline.innerHTML = '<div class="empty-timeline">No resilience events logged yet.</div>';
                    return;
                }

                let html = '';
                data.forEach(ev => {
                    html += `
                        <div class="timeline-item">
                            <div class="timeline-dot dot-${ev.type}"></div>
                            <div class="timeline-content">
                                <div class="timeline-header">
                                    <span class="timeline-badge badge-${ev.type}">${ev.type.replace('_', ' ')}</span>
                                    <span>${ev.timestamp}</span>
                                </div>
                                <div class="timeline-msg">${ev.message} <span style="color: var(--text-secondary); font-size: 0.8rem;">(Provider: ${ev.provider})</span></div>
                            </div>
                        </div>
                    `;
                });
                timeline.innerHTML = html;
            })
            .catch(err => console.error("Error fetching events:", err));
    }

    function flushCache() {
        if (!confirm("Are you sure you want to flush all cache levels? This will force reload queries from the database.")) {
            return;
        }

        fetch('/api/cache/clear', { method: 'POST' })
            .then(res => res.json())
            .then(data => {
                showToast(data.message || "All caches cleared successfully!");
                fetchStats();
                fetchEvents();
            })
            .catch(err => {
                console.error("Error flushing cache:", err);
                showToast("Failed to clear cache.", true);
            });
    }

    function showToast(msg, isError = false) {
        const toast = document.getElementById('toast-alert');
        const messageEl = document.getElementById('toast-message');
        const icon = toast.querySelector('.toast-icon');

        messageEl.textContent = msg;
        if (isError) {
            toast.style.borderColor = 'var(--danger)';
            icon.style.backgroundColor = 'var(--danger)';
            icon.textContent = '!';
        } else {
            toast.style.borderColor = 'var(--success)';
            icon.style.backgroundColor = 'var(--success)';
            icon.textContent = '✓';
        }

        toast.classList.add('toast-active');
        setTimeout(() => {
            toast.classList.remove('toast-active');
        }, 3000);
    }

    // Trigger initial load and set intervals
    fetchHealth();
    fetchStats();
    fetchEvents();

    setInterval(fetchHealth, 2000);
    setInterval(fetchStats, 2000);
    setInterval(fetchEvents, 2000);
</script>
</body>
</html>

```

---

## DemoApplication.java (Demo Application Main)

### File Path: `resilient-cache-demo/src/main/java/com/resilientcache/demo/DemoApplication.java`

### 💡 What this code does (in simple language):
Main startup class of the demo app. Seeds PostgreSQL/H2 tables with sample product records (IDs 100, 200, 300) at startup if empty.

### 💻 Source Code:
```java
package com.resilientcache.demo;

import com.resilientcache.demo.model.Product;
import com.resilientcache.demo.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /**
     * Seeds sample products into the PostgreSQL/H2 database at startup if empty.
     */
    @Bean
    public CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.save(new Product(
                        100L, 
                        "Premium Widget 100", 
                        99.99, 
                        "A high-quality resilient enterprise widget."
                ));
                productRepository.save(new Product(
                        200L, 
                        "Transient Widget 200", 
                        19.99, 
                        "A short-lived widget suitable for transient caching."
                ));
                productRepository.save(new Product(
                        300L, 
                        "Ultimate Gadget 300", 
                        249.50, 
                        "The flagship gadget with premium performance components."
                ));
                System.out.println(">>> Seeded mock database with 3 sample products.");
            } else {
                System.out.println(">>> Database already contains product data. Skipping seed.");
            }
        };
    }
}

```

---

## Product.java (JPA Database Entity)

### File Path: `resilient-cache-demo/src/main/java/com/resilientcache/demo/model/Product.java`

### 💡 What this code does (in simple language):
Relational JPA database entity mapping database rows in the products table to Java variables. Implements Serializable to support cache transfers.

### 💻 Source Code:
```java
package com.resilientcache.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "products")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;
    private String name;
    private Double price;
    private String description;

    public Product() {
    }

    public Product(Long id, String name, Double price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Product[ID=" + id + ", Name=" + name + ", Price=$" + price + ", Desc=" + description + "]";
    }
}

```

---

## ProductRepository.java (Spring Data JPA Interface)

### File Path: `resilient-cache-demo/src/main/java/com/resilientcache/demo/repository/ProductRepository.java`

### 💡 What this code does (in simple language):
Repository interface extending Spring Data JPA JpaRepository to execute SELECT, INSERT, and DELETE database queries automatically.

### 💻 Source Code:
```java
package com.resilientcache.demo.repository;

import com.resilientcache.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}

```

---

## ProductService.java (Simulated Database Service)

### File Path: `resilient-cache-demo/src/main/java/com/resilientcache/demo/service/ProductService.java`

### 💡 What this code does (in simple language):
Query service executing relational table queries on cache misses. Decorated with @ResilientCache annotations.

### 💻 Source Code:
```java
package com.resilientcache.demo.service;

import com.resilientcache.annotation.ResilientCache;
import com.resilientcache.demo.model.Product;
import com.resilientcache.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Retrieves product details. Simulates database query delay of 1 second.
     * Caches value with default properties TTL.
     */
    @ResilientCache(name = "products", key = "#id")
    public Product getProductById(Long id) {
        simulateDatabaseDelay();
        return productRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves product details with a custom short TTL override (5 seconds).
     * Simulates database query delay of 1 second.
     */
    @ResilientCache(name = "products", key = "#id", ttl = "5s")
    public Product getProductWithShortTtl(Long id) {
        simulateDatabaseDelay();
        return productRepository.findById(id).orElse(null);
    }

    private void simulateDatabaseDelay() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Simulated database query interrupted", e);
        }
    }
}

```

---

## ProductController.java (REST API Endpoint)

### File Path: `resilient-cache-demo/src/main/java/com/resilientcache/demo/controller/ProductController.java`

### 💡 What this code does (in simple language):
Exposes REST endpoints to query products by ID, returning product details and measured lookup latencies to indicate cache hits/misses.

### 💻 Source Code:
```java
package com.resilientcache.demo.controller;

import com.resilientcache.demo.model.Product;
import com.resilientcache.demo.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Exposes endpoint to fetch a product by ID. Returns the product data and measurement of latency.
     */
    @GetMapping("/{id}")
    public Map<String, Object> getProduct(@PathVariable Long id) {
        long start = System.currentTimeMillis();
        Product data = productService.getProductById(id);
        long duration = System.currentTimeMillis() - start;

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("executionTimeMs", duration);
        response.put("cached", duration < 50); // if it takes < 50ms, it is served from cache
        return response;
    }

    /**
     * Exposes endpoint to fetch transient product by ID (short TTL).
     */
    @GetMapping("/transient/{id}")
    public Map<String, Object> getProductTransient(@PathVariable Long id) {
        long start = System.currentTimeMillis();
        Product data = productService.getProductWithShortTtl(id);
        long duration = System.currentTimeMillis() - start;

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("executionTimeMs", duration);
        response.put("cached", duration < 50);
        return response;
    }
}

```

---

## application.yml (Demo Application Configuration)

### File Path: `resilient-cache-demo/src/main/resources/application.yml`

### 💡 What this code does (in simple language):
Configures demo application parameters: connects to a local PostgreSQL server (username/password: postgres), enables Hibernate auto-update DDL, activates multi-level cache, and exposes Spring Boot Actuator endpoints.

### 💻 Source Code:
```yaml
server:
  port: 8080

logging:
  level:
    root: info
    com.resilientcache: info

spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000
  datasource:
    url: jdbc:postgresql://localhost:5432/resilient_cache_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always

resilient-cache:
  enabled: true
  provider: multi-level
  default-ttl: 30m
  stampede-protection: true
  caffeine:
    maximum-size: 10000
    expire-after-write: 15m



```

---

## application-test.yml (Test Properties)

### File Path: `resilient-cache-demo/src/test/resources/application-test.yml`

### 💡 What this code does (in simple language):
Properties configuration for test executions. Automatically overrides datasource url to use H2 in-memory memory database (jdbc:h2:mem:testdb) and sets cache provider to caffeine to run offline without database dependencies.

### 💻 Source Code:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  data:
    redis:
      host: localhost
      port: 6379

resilient-cache:
  enabled: true
  provider: caffeine
  default-ttl: 5m
  caffeine:
    maximum-size: 1000
    expire-after-write: 5m

```

---

## benchmark.py (Profiling Performance Script)

### File Path: `../../.gemini/antigravity/brain/65792529-fa19-4f80-bbeb-9adbe9f8e6ae/scratch/benchmark.py`

### 💡 What this code does (in simple language):
Benchmark profiling script that runs load cycles against the caching server, computing latencies, percentiles (p50, p95, p99), and writing benchmark summaries to benchmark_results.md.

### 💻 Source Code:
*File not found at `../../.gemini/antigravity/brain/65792529-fa19-4f80-bbeb-9adbe9f8e6ae/scratch/benchmark.py`*

---

## MultiLevelCacheAdapterTest.java (Mockito Unit Tests)

### File Path: `resilient-cache-starter/src/test/java/com/resilientcache/provider/multi/MultiLevelCacheAdapterTest.java`

### 💡 What this code does (in simple language):
A comprehensive JUnit test suite. It uses Mockito to mock Caffeine and Redis adapters, testing 7 core multi-level lookup paths (including L1 hits, L2 hits, back-propagation of remaining TTL, and write-through/eviction flows) in isolation from external services.

### 💻 Source Code:
```java
package com.resilientcache.provider.multi;

import com.resilientcache.provider.caffeine.CaffeineAdapter;
import com.resilientcache.provider.redis.RedisAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiLevelCacheAdapterTest {

    private CaffeineAdapter caffeineAdapter;
    private RedisAdapter redisAdapter;
    private RedisTemplate<String, Object> redisTemplate;
    private MultiLevelCacheAdapter adapter;

    @BeforeEach
    void setUp() {
        caffeineAdapter = mock(CaffeineAdapter.class);
        redisAdapter = mock(RedisAdapter.class);
        redisTemplate = mock(RedisTemplate.class);
        adapter = new MultiLevelCacheAdapter(caffeineAdapter, redisAdapter, redisTemplate);
    }

    @Test
    void testGetL1Hit() {
        String key = "testKey";
        String value = "l1Value";
        when(caffeineAdapter.get(key)).thenReturn(Optional.of(value));

        Optional<Object> result = adapter.get(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());
        verify(caffeineAdapter, times(1)).get(key);
        verifyNoInteractions(redisAdapter);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void testGetL1MissL2HitWithTtl() {
        String key = "testKey";
        String value = "l2Value";
        when(caffeineAdapter.get(key)).thenReturn(Optional.empty());
        when(redisAdapter.get(key)).thenReturn(Optional.of(value));
        when(redisTemplate.getExpire(key, TimeUnit.SECONDS)).thenReturn(120L);

        Optional<Object> result = adapter.get(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());
        verify(caffeineAdapter, times(1)).get(key);
        verify(redisAdapter, times(1)).get(key);
        verify(redisTemplate, times(1)).getExpire(eq(expireKey(key)), eq(TimeUnit.SECONDS));
        verify(caffeineAdapter, times(1)).put(key, value, Duration.ofSeconds(120));


    }

    @Test
    void testGetL1MissL2HitWithNoTtl() {
        String key = "testKey";
        String value = "l2Value";
        when(caffeineAdapter.get(key)).thenReturn(Optional.empty());
        when(redisAdapter.get(key)).thenReturn(Optional.of(value));
        when(redisTemplate.getExpire(key, TimeUnit.SECONDS)).thenReturn(-1L); // -1 means no TTL in Redis

        Optional<Object> result = adapter.get(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());
        verify(caffeineAdapter, times(1)).get(key);
        verify(redisAdapter, times(1)).get(key);
        verify(caffeineAdapter, times(1)).put(key, value, null); // should fall back to default L1 TTL
    }

    @Test
    void testGetL1MissL2Miss() {
        String key = "testKey";
        when(caffeineAdapter.get(key)).thenReturn(Optional.empty());
        when(redisAdapter.get(key)).thenReturn(Optional.empty());

        Optional<Object> result = adapter.get(key);

        assertFalse(result.isPresent());
        verify(caffeineAdapter, times(1)).get(key);
        verify(redisAdapter, times(1)).get(key);
    }

    @Test
    void testPutWritesThroughBoth() {
        String key = "testKey";
        String value = "newValue";
        Duration ttl = Duration.ofMinutes(10);

        adapter.put(key, value, ttl);

        verify(caffeineAdapter, times(1)).put(key, value, ttl);
        verify(redisAdapter, times(1)).put(key, value, ttl);
    }

    @Test
    void testEvictDeletesFromBoth() {
        String key = "testKey";

        adapter.evict(key);

        verify(caffeineAdapter, times(1)).evict(key);
        verify(redisAdapter, times(1)).evict(key);
    }

    @Test
    void testClearClearsBoth() {
        adapter.clear();

        verify(caffeineAdapter, times(1)).clear();
        verify(redisAdapter, times(1)).clear();
    }

    private String expireKey(String key) {
        return key;
    }
}

```

---

## HealthSchedulerTest.java (Mockito Unit Tests)

### File Path: `resilient-cache-starter/src/test/java/com/resilientcache/monitoring/HealthSchedulerTest.java`

### 💡 What this code does (in simple language):
Validates the health monitoring scheduled task. It mocks the cache provider to return true/false, verifying status updates and correct broadcasting of HealthStatusChangedEvent and ProviderFailureEvent.

### 💻 Source Code:
```java
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

```

---

## RecoveryManagerTest.java (Mockito Unit Tests)

### File Path: `resilient-cache-starter/src/test/java/com/resilientcache/recovery/RecoveryManagerTest.java`

### 💡 What this code does (in simple language):
Validates the self-healing reconnection retry routine. It tests immediate recovery success, multiple failed attempts followed by connection restoration, and complete exhaustion failures triggering custom exceptions.

### 💻 Source Code:
```java
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

```

---

## WarmupServiceTest.java (Mockito Unit Tests)

### File Path: `resilient-cache-starter/src/test/java/com/resilientcache/warmup/WarmupServiceTest.java`

### 💡 What this code does (in simple language):
Validates that WarmupService intercepts RecoveryCompletedEvent, checking that it preloads hot keys to the active cache provider when enabled, and ignores events when disabled.

### 💻 Source Code:
```java
package com.resilientcache.warmup;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.event.RecoveryCompletedEvent;
import com.resilientcache.port.CachePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class WarmupServiceTest {

    private CachePort activeCachePort;
    private CacheProperties properties;
    private WarmupService warmupService;

    @BeforeEach
    void setUp() {
        activeCachePort = mock(CachePort.class);
        properties = new CacheProperties();
        properties.getWarmup().setEnabled(true);
        warmupService = new WarmupService(activeCachePort, properties);
    }

    @Test
    void testWarmupPreloadsMockKeysOnRecoveryEvent() {
        RecoveryCompletedEvent event = new RecoveryCompletedEvent("redis", 1, 100);

        warmupService.onRecoveryCompleted(event);

        verify(activeCachePort, times(1)).put(eq("products:100"), any(), eq(properties.getDefaultTtl()));
        verify(activeCachePort, times(1)).put(eq("products:200"), any(), eq(properties.getDefaultTtl()));
    }

    @Test
    void testWarmupDoesNotTriggerIfDisabled() {
        properties.getWarmup().setEnabled(false);
        RecoveryCompletedEvent event = new RecoveryCompletedEvent("redis", 1, 100);

        warmupService.onRecoveryCompleted(event);

        verifyNoInteractions(activeCachePort);
    }
}

```

---

## CacheMonitorControllerTest.java (Mockito Unit Tests)

### File Path: `resilient-cache-starter/src/test/java/com/resilientcache/controller/CacheMonitorControllerTest.java`

### 💡 What this code does (in simple language):
Tests the CacheMonitorController endpoint outputs, verifying stats maps, engine health objects, event arrays, and clearing routines.

### 💻 Source Code:
```java
package com.resilientcache.controller;

import com.resilientcache.config.CacheProperties;
import com.resilientcache.core.CacheManager;
import com.resilientcache.event.HealthStatus;
import com.resilientcache.monitoring.EventTracker;
import com.resilientcache.monitoring.HealthScheduler;
import com.resilientcache.monitoring.MetricsTracker;
import com.resilientcache.provider.caffeine.CaffeineAdapter;
import com.resilientcache.provider.redis.RedisAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheMonitorControllerTest {

    private CaffeineAdapter caffeineAdapter;
    private RedisAdapter redisAdapter;
    private MetricsTracker metricsTracker;
    private EventTracker eventTracker;
    private CacheManager cacheManager;
    private CacheProperties properties;
    private HealthScheduler healthScheduler;
    private CacheMonitorController controller;

    @BeforeEach
    void setUp() {
        caffeineAdapter = mock(CaffeineAdapter.class);
        redisAdapter = mock(RedisAdapter.class);
        metricsTracker = mock(MetricsTracker.class);
        eventTracker = mock(EventTracker.class);
        cacheManager = mock(CacheManager.class);
        properties = new CacheProperties();
        properties.setProvider("multi-level");
        healthScheduler = mock(HealthScheduler.class);
        
        controller = new CacheMonitorController(
            caffeineAdapter,
            redisAdapter,
            metricsTracker,
            eventTracker,
            cacheManager,
            properties,
            healthScheduler
        );
    }

    @Test
    void testGetStats() {
        com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats = mock(com.github.benmanes.caffeine.cache.stats.CacheStats.class);
        when(caffeineStats.hitCount()).thenReturn(10L);
        when(caffeineStats.missCount()).thenReturn(5L);
        when(caffeineStats.hitRate()).thenReturn(0.66);
        when(caffeineAdapter.getStats()).thenReturn(caffeineStats);

        when(metricsTracker.getL2HitCount()).thenReturn(20L);
        when(metricsTracker.getL2MissCount()).thenReturn(10L);
        when(metricsTracker.getAverageReadLatencyMs()).thenReturn(1.5);
        when(metricsTracker.getAverageWriteLatencyMs()).thenReturn(3.2);

        ResponseEntity<Map<String, Object>> response = controller.getStats();
        Map<String, Object> body = response.getBody();

        assertNotNull(body);
        assertEquals("multi-level", body.get("provider"));
        assertEquals(10L, body.get("l1Hits"));
        assertEquals(5L, body.get("l1Misses"));
        assertEquals(0.66, body.get("l1HitRatio"));
        assertEquals(20L, body.get("l2Hits"));
        assertEquals(10L, body.get("l2Misses"));
        assertEquals(20.0 / 30.0, body.get("l2HitRatio"));
        assertEquals(1.5, body.get("averageReadLatencyMs"));
        assertEquals(3.2, body.get("averageWriteLatencyMs"));
    }

    @Test
    void testGetHealth() {
        when(healthScheduler.getCurrentStatus()).thenReturn(HealthStatus.UP);
        when(redisAdapter.healthCheck()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.getHealth();
        Map<String, Object> body = response.getBody();

        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertEquals("UP", body.get("l1Status"));
        assertEquals("UP", body.get("l2Status"));
    }

    @Test
    void testGetEvents() {
        List<Map<String, Object>> mockEvents = new ArrayList<>();
        Map<String, Object> mockEvent = new HashMap<>();
        mockEvent.put("type", "FAILURE");
        mockEvents.add(mockEvent);
        when(eventTracker.getEvents()).thenReturn(mockEvents);

        ResponseEntity<List<Map<String, Object>>> response = controller.getEvents();
        List<Map<String, Object>> body = response.getBody();

        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("FAILURE", body.get(0).get("type"));
    }

    @Test
    void testClearCache() {
        ResponseEntity<Map<String, String>> response = controller.clearCache();
        Map<String, String> body = response.getBody();

        assertNotNull(body);
        assertEquals("Cache cleared successfully", body.get("message"));
        verify(cacheManager, times(1)).clear();
        verify(eventTracker, times(1)).addManualEvent(eq("CACHE_CLEAR"), eq("multi-level"), anyString());
    }
}

```

---

## ProductControllerIntegrationTest.java (Integration Test)

### File Path: `resilient-cache-demo/src/test/java/com/resilientcache/demo/ProductControllerIntegrationTest.java`

### 💡 What this code does (in simple language):
End-to-end integration test. Connects to H2 in-memory database, hitting endpoints to verify cache misses, cache hits, database queries, and verifying that database deletions do not impact the cached entity lookup (proving memory serving).

### 💻 Source Code:
```java
package com.resilientcache.demo;

import com.resilientcache.demo.model.Product;
import com.resilientcache.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void cleanUp() {
        productRepository.deleteAll();
        // Seed default product for test runs
        productRepository.save(new Product(
                100L, 
                "Premium Widget 100", 
                99.99, 
                "Integration Test Widget"
        ));
    }

    @Test
    void testEndToEndCachingFlow() {
        String url = getBaseUrl() + "/products/100";

        // --- 1. First Call: Cache Miss ---
        long start = System.currentTimeMillis();
        Map<String, Object> response1 = restTemplate.getForObject(url, Map.class);
        long elapsed1 = System.currentTimeMillis() - start;

        assertNotNull(response1);
        Map<String, Object> data1 = (Map<String, Object>) response1.get("data");
        assertNotNull(data1);
        assertEquals("Premium Widget 100", data1.get("name"));
        assertEquals(99.99, data1.get("price"));
        assertFalse((Boolean) response1.get("cached"), "First hit should not be cached");

        // --- 2. Second Call: Cache Hit ---
        Map<String, Object> response2 = restTemplate.getForObject(url, Map.class);
        assertNotNull(response2);
        assertTrue((Boolean) response2.get("cached"), "Second hit should be cached");
        
        Map<String, Object> data2 = (Map<String, Object>) response2.get("data");
        assertEquals("Premium Widget 100", data2.get("name"));

        // --- 3. Database Outage/Deletion Decoupling Check ---
        // Delete the record from the database. Since it is cached, the engine should still serve it!
        productRepository.deleteAll();

        Map<String, Object> response3 = restTemplate.getForObject(url, Map.class);
        assertNotNull(response3);
        assertTrue((Boolean) response3.get("cached"), "Should still serve from cache after DB deletion");
        
        Map<String, Object> data3 = (Map<String, Object>) response3.get("data");
        assertEquals("Premium Widget 100", data3.get("name"));
    }
}

```

---

