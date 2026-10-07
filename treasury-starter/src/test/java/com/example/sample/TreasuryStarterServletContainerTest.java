package com.example.sample;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Runs against a real servlet container so that error dispatches to /error go through the security chain,
 * which MockMvc does not exercise.
 */
@SpringBootTest(classes = SampleServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TreasuryStarterServletContainerTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void missingPublicResourceIsNotFoundForAnonymousCallers() {
        ResponseEntity<String> response = rest.getForEntity("/swagger-ui.html", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void unknownApiPathIsNotFoundForAuthenticatedViewer() {
        ResponseEntity<String> response = rest.withBasicAuth("treasury-viewer", "viewer-local")
                .getForEntity("/api/does-not-exist", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void anonymousApiCallIsStillChallenged() {
        ResponseEntity<String> response = rest.getForEntity("/api/ping", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getFirst("WWW-Authenticate")).startsWith("Basic");
    }
}
