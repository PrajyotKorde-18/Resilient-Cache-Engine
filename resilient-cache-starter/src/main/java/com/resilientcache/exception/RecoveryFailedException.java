package com.resilientcache.exception;

public class RecoveryFailedException extends CacheEngineException {

    public RecoveryFailedException(String message) {
        super(message, "CACHE_103");
    }

    public RecoveryFailedException(String message, Throwable cause) {
        super(message, cause, "CACHE_103");
    }
}
