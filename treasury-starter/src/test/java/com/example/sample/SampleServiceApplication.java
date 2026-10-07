package com.example.sample;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/** A minimal consumer of treasury-starter, outside the platform package, as a real service would be. */
@SpringBootApplication
public class SampleServiceApplication {

    @RestController
    static class SampleController {
        @GetMapping("/api/ping")
        Map<String, String> ping() {
            return Collections.singletonMap("status", "ok");
        }

        @PostMapping("/api/transfers")
        Map<String, String> transfer() {
            return Collections.singletonMap("status", "accepted");
        }

        @GetMapping("/api/conflict")
        Map<String, String> conflict() {
            throw new IllegalStateException("Insufficient available balance");
        }

        @GetMapping("/docs/info")
        Map<String, String> docs() {
            return Collections.singletonMap("service", "sample");
        }
    }
}
