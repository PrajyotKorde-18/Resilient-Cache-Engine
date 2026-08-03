package com.resilientcache.exception;

public class ProviderNotConfiguredException extends CacheEngineException {

    public ProviderNotConfiguredException(String message) {
        super(message, "CACHE_101");
    }

    public ProviderNotConfiguredException(String message, Throwable cause) {
        super(message, cause, "CACHE_101");
    }
}
