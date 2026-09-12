package com.example.fullstacktest.config;

import com.example.fullstacktest.item.LabItem;
import com.example.fullstacktest.item.LabItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataConfig {
    @Bean
    CommandLineRunner addExampleItem(LabItemRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new LabItem("First database record", "Created by Spring Boot at startup."));
            }
        };
    }
}
