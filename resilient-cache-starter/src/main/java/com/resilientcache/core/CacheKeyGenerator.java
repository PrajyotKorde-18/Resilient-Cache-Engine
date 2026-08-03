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
