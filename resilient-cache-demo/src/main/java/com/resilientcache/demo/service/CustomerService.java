package com.resilientcache.demo.service;

import com.resilientcache.annotation.ResilientCache;
import com.resilientcache.demo.model.Customer;
import com.resilientcache.demo.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Retrieves customer by index with dynamic caching support.
     * Simulates database delay of 1 second on cache miss.
     */
    @ResilientCache(name = "customers", key = "#index")
    public Customer getCustomerByIndex(Long index) {
        simulateDatabaseDelay();
        return customerRepository.findById(index).orElse(null);
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
