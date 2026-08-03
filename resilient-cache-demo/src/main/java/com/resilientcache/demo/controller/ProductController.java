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
    public Map<String, Object> getProduct(@PathVariable String id) {
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
    public Map<String, Object> getProductTransient(@PathVariable String id) {
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
