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
