package com.example.sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs on a real servlet container: MockMvc does not perform the ERROR dispatch Security 6 now authorizes. */
@SpringBootTest(classes = SampleServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TreasuryStarterErrorDispatchTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void missingPublicResourceIsNotFoundForAnonymousCallers() {
        ResponseEntity<String> response = rest.getForEntity("/docs/missing", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void missingApiResourceIsNotFoundForAuthenticatedCallers() {
        ResponseEntity<String> response = rest.withBasicAuth("treasury-viewer", "viewer-local")
                .getForEntity("/api/missing", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
