package com.example.sample;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

        @GetMapping("/api/status")
        Map<String, String> status() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sample resource is missing");
        }

        @GetMapping("/api/unhandled")
        Map<String, String> unhandled() {
            throw new UnsupportedOperationException("Unexpected sample failure");
        }

        @PostMapping("/api/validated")
        Map<String, String> validated(@Valid @RequestBody SampleRequest request) {
            return Collections.singletonMap("name", request.name);
        }

        @GetMapping("/docs/info")
        Map<String, String> docs() {
            return Collections.singletonMap("service", "sample");
        }
    }

    static class SampleRequest {
        @NotBlank(message = "name must not be blank")
        public String name;
    }
}
