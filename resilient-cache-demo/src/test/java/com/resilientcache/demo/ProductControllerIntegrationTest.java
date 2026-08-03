package com.resilientcache.demo;

import com.resilientcache.demo.model.Product;
import com.resilientcache.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void cleanUp() {
        productRepository.deleteAll();
        // Seed default product for test runs
        productRepository.save(new Product(
                100L, 
                "Premium Widget 100", 
                99.99, 
                "Integration Test Widget"
        ));
    }

    @Test
    void testEndToEndCachingFlow() {
        String url = getBaseUrl() + "/products/100";

        // --- 1. First Call: Cache Miss ---
        long start = System.currentTimeMillis();
        Map<String, Object> response1 = restTemplate.getForObject(url, Map.class);
        long elapsed1 = System.currentTimeMillis() - start;

        assertNotNull(response1);
        Map<String, Object> data1 = (Map<String, Object>) response1.get("data");
        assertNotNull(data1);
        assertEquals("Premium Widget 100", data1.get("name"));
        assertEquals(99.99, data1.get("price"));
        assertFalse((Boolean) response1.get("cached"), "First hit should not be cached");

        // --- 2. Second Call: Cache Hit ---
        Map<String, Object> response2 = restTemplate.getForObject(url, Map.class);
        assertNotNull(response2);
        assertTrue((Boolean) response2.get("cached"), "Second hit should be cached");
        
        Map<String, Object> data2 = (Map<String, Object>) response2.get("data");
        assertEquals("Premium Widget 100", data2.get("name"));

        // --- 3. Database Outage/Deletion Decoupling Check ---
        // Delete the record from the database. Since it is cached, the engine should still serve it!
        productRepository.deleteAll();

        Map<String, Object> response3 = restTemplate.getForObject(url, Map.class);
        assertNotNull(response3);
        assertTrue((Boolean) response3.get("cached"), "Should still serve from cache after DB deletion");
        
        Map<String, Object> data3 = (Map<String, Object>) response3.get("data");
        assertEquals("Premium Widget 100", data3.get("name"));
    }
}
