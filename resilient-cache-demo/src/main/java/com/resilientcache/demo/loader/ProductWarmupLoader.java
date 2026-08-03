package com.resilientcache.demo.loader;

import com.resilientcache.demo.repository.ProductRepository;
import com.resilientcache.port.CacheWarmupLoader;
import org.springframework.stereotype.Component;

@Component
public class ProductWarmupLoader implements CacheWarmupLoader {

    private final ProductRepository repository;

    public ProductWarmupLoader(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean supports(String cacheName) {
        return "products".equals(cacheName);
    }

    @Override
    public Object load(String key) {
        int separatorIdx = key.indexOf(':');
        if (separatorIdx <= 0) {
            return null;
        }
        String id = key.substring(separatorIdx + 1);
        return repository.findById(id).orElse(null);
    }
}
