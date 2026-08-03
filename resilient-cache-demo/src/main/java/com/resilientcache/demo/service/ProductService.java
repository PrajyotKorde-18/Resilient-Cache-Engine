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
    public Product getProductById(String id) {
        simulateDatabaseDelay();
        return productRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves product details with a custom short TTL override (5 seconds).
     * Simulates database query delay of 1 second.
     */
    @ResilientCache(name = "products", key = "#id", ttl = "5s")
    public Product getProductWithShortTtl(String id) {
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
