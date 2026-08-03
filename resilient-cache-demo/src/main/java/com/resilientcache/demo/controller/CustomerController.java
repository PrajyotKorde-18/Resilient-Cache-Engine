package com.resilientcache.demo.controller;

import com.resilientcache.demo.model.Customer;
import com.resilientcache.demo.service.CustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Exposes endpoint to fetch customer by index. Returns details and measured lookup latency.
     */
    @GetMapping("/{index}")
    public Map<String, Object> getCustomer(@PathVariable Long index) {
        long start = System.currentTimeMillis();
        Customer data = customerService.getCustomerByIndex(index);
        long duration = System.currentTimeMillis() - start;

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("executionTimeMs", duration);
        response.put("cached", duration < 50);
        return response;
    }
}
