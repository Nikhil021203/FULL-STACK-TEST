package com.example.fullstacktest.api;

import com.example.fullstacktest.item.LabItemRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {
    private final LabItemRepository repository;

    public ApiController(LabItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/status")
    public Map<String, Object> status() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("backend", "Spring Boot is running");
        response.put("database", "PostgreSQL reachable through JPA/Hibernate");
        response.put("itemCount", repository.count()); // proves a database query worked
        response.put("time", Instant.now());
        return response;
    }

    @GetMapping("/api/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from Java → Spring Boot → REST API!");
    }

    @GetMapping("/api/auth/me")
    public Map<String, Object> currentUser(Authentication authentication) {
        boolean signedIn = authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        return Map.of("authenticated", signedIn, "username", signedIn ? authentication.getName() : "guest");
    }

    @GetMapping("/api/protected/message")
    public Map<String, String> protectedMessage(Authentication authentication) {
        return Map.of("message", "You reached a protected Spring Security endpoint, " + authentication.getName() + "!");
    }
}
