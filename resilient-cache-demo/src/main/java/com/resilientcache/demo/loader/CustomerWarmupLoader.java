package com.resilientcache.demo.loader;

import com.resilientcache.demo.model.Customer;
import com.resilientcache.demo.repository.CustomerRepository;
import com.resilientcache.port.CacheWarmupLoader;
import org.springframework.stereotype.Component;

@Component
public class CustomerWarmupLoader implements CacheWarmupLoader {

    private final CustomerRepository repository;

    public CustomerWarmupLoader(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean supports(String cacheName) {
        return "customers".equals(cacheName);
    }

    @Override
    public Object load(String key) {
        int separatorIdx = key.indexOf(':');
        if (separatorIdx <= 0) {
            return null;
        }
        String indexStr = key.substring(separatorIdx + 1);
        try {
            Long index = Long.parseLong(indexStr);
            return repository.findById(index).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
