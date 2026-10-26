package com.example.sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SampleServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class RealContainerSecurityResponseTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void recordsRealContainerResponsesForSecurityAndErrorDispatches() {
        ResponseEntity<String> anonymous = restTemplate.getForEntity("/api/ping", String.class);
        ResponseEntity<String> badPassword = restTemplate.withBasicAuth("treasury-viewer", "wrong-password")
                .getForEntity("/api/ping", String.class);
        ResponseEntity<String> viewerPost = restTemplate.withBasicAuth("treasury-viewer", "viewer-local")
                .postForEntity("/api/transfers", null, String.class);
        ResponseEntity<String> missingApi = restTemplate.withBasicAuth("treasury-viewer", "viewer-local")
                .getForEntity("/api/nope", String.class);
        ResponseEntity<String> unhandled = restTemplate.withBasicAuth("treasury-viewer", "viewer-local")
                .getForEntity("/api/unhandled", String.class);
        ResponseEntity<String> missingPublic = restTemplate.getForEntity("/docs/nope", String.class);

        assertSecurityHeaders(anonymous);
        assertThat(anonymous.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(anonymous.getHeaders().getFirst("Vary")).isNull();
        assertThat(anonymous.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE))
                .isEqualTo("Basic realm=\"Realm\"");
        assertThat(anonymous.getHeaders().getContentType()).isNull();
        assertThat(anonymous.getBody()).isNullOrEmpty();

        assertSecurityHeaders(badPassword);
        assertThat(badPassword.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(badPassword.getHeaders().getFirst("Vary")).isNull();
        assertThat(badPassword.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE))
                .isEqualTo("Basic realm=\"Realm\"");
        assertThat(badPassword.getHeaders().getContentType()).isNull();
        assertThat(badPassword.getBody()).isNullOrEmpty();

        assertSecurityHeaders(viewerPost);
        assertThat(viewerPost.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(viewerPost.getHeaders().getFirst("Vary")).isNull();
        assertThat(viewerPost.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).isNull();
        assertThat(viewerPost.getHeaders().getContentType()).isNull();
        assertThat(viewerPost.getBody()).isNullOrEmpty();

        assertSecurityHeaders(missingApi);
        assertThat(missingApi.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(missingApi.getHeaders().getFirst("Vary")).isEqualTo("Origin");
        assertBootError(missingApi, 404, "Not Found", "/api/nope");

        assertSecurityHeaders(unhandled);
        assertThat(unhandled.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(unhandled.getHeaders().getFirst("Vary")).isNull();
        assertBootError(unhandled, 500, "Internal Server Error", "/api/unhandled");

        assertSecurityHeaders(missingPublic);
        assertThat(missingPublic.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(missingPublic.getHeaders().getFirst("Vary")).isEqualTo("Origin");
        assertBootError(missingPublic, 404, "Not Found", "/docs/nope");
    }

    private void assertSecurityHeaders(ResponseEntity<String> response) {
        assertThat(response.getHeaders().getFirst("X-XSS-Protection")).isEqualTo("1; mode=block");
        assertThat(response.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeaders().getFirst("X-Frame-Options")).isEqualTo("SAMEORIGIN");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL))
                .isEqualTo("no-cache, no-store, max-age=0, must-revalidate");
        assertThat(response.getHeaders().getFirst(HttpHeaders.PRAGMA)).isEqualTo("no-cache");
        assertThat(response.getHeaders().getFirst(HttpHeaders.EXPIRES)).isEqualTo("0");
        assertThat(response.getHeaders().getFirst("Strict-Transport-Security")).isNull();
    }

    private void assertBootError(ResponseEntity<String> response, int status, String error, String path) {
        assertThat(response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).isNull();
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody()).matches("\\{\"timestamp\":\"[^\"]+\",\"status\":" + status
                + ",\"error\":\"" + error + "\",\"path\":\"" + path + "\"\\}");
    }
}
