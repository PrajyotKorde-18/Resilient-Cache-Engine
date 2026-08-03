package com.resilientcache.demo;

import com.resilientcache.demo.model.Product;
import com.resilientcache.demo.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /**
     * Seeds sample products into the PostgreSQL/H2 database at startup if empty.
     */
//    @Bean
//    public CommandLineRunner initDatabase(ProductRepository productRepository) {
//        return args -> {
//            if (productRepository.count() == 0) {
//                productRepository.save(new Product(
//                        100L, 
//                        "Premium Widget 100", 
//                        99.99, 
//                        "A high-quality resilient enterprise widget."
//                ));
//                productRepository.save(new Product(
//                        200L, 
//                        "Transient Widget 200", 
//                        19.99, 
//                        "A short-lived widget suitable for transient caching."
//                ));
//                productRepository.save(new Product(
//                        300L, 
//                        "Ultimate Gadget 300", 
//                        249.50, 
//                        "The flagship gadget with premium performance components."
//                ));
//                System.out.println(">>> Seeded mock database with 3 sample products.");
//            } else {
//                System.out.println(">>> Database already contains product data. Skipping seed.");
//            }
//        };
//    }
}
