package com.example.sample;

import com.harborline.treasury.platform.web.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SampleServiceApplication.class)
@AutoConfigureMockMvc
class TreasuryStarterIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApplicationContext context;

    @Test
    void starterIsDiscoveredAndSecuresConsumerWithoutSecurityConfiguration() throws Exception {
        assertThat(context.getBeansOfType(CorrelationIdFilter.class)).hasSize(1);
        assertUnauthorized(mvc.perform(get("/api/ping")).andReturn());
    }

    @Test
    void anonymousRequestsReceiveBasicChallenge() throws Exception {
        assertUnauthorized(mvc.perform(get("/api/ping")).andReturn());
        assertUnauthorized(mvc.perform(post("/api/transfers")).andReturn());
    }

    @Test
    void viewerCanReadAndReceivesGeneratedCorrelationId() throws Exception {
        mvc.perform(get("/api/ping").with(httpBasic("treasury-viewer", "viewer-local")))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", org.hamcrest.Matchers.matchesPattern(
                        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
                .andExpect(content().json("{\"status\":\"ok\"}"));
    }

    @Test
    void callerCorrelationIdIsPropagated() throws Exception {
        mvc.perform(get("/api/ping").header("X-Correlation-Id", "trace-123")
                        .with(httpBasic("treasury-viewer", "viewer-local")))
                .andExpect(header().string("X-Correlation-Id", "trace-123"));
    }

    @Test
    void viewerCannotChangeState() throws Exception {
        mvc.perform(post("/api/transfers").with(httpBasic("treasury-viewer", "viewer-local")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    void operatorCanChangeStateWithoutCsrfToken() throws Exception {
        mvc.perform(post("/api/transfers").with(httpBasic("treasury-operator", "operator-local")))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"accepted\"}"));
    }

    @Test
    void badPasswordReceivesBasicChallenge() throws Exception {
        assertUnauthorized(mvc.perform(get("/api/ping").with(httpBasic("treasury-viewer", "wrong-password")))
                .andReturn());
    }

    @Test
    void businessRuleViolationUsesSharedErrorEnvelope() throws Exception {
        mvc.perform(get("/api/conflict").header("X-Correlation-Id", "trace-409")
                        .with(httpBasic("treasury-viewer", "viewer-local")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Insufficient available balance"))
                .andExpect(jsonPath("$.correlationId").value("trace-409"));
    }

    @Test
    void responseStatusExceptionUsesCompleteSharedErrorEnvelope() throws Exception {
        mvc.perform(get("/api/status").header("X-Correlation-Id", "trace-404")
                        .with(httpBasic("treasury-viewer", "viewer-local")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Sample resource is missing"))
                .andExpect(jsonPath("$.correlationId").value("trace-404"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").doesNotExist());
    }

    @Test
    void validationErrorUsesCompleteSharedErrorEnvelope() throws Exception {
        mvc.perform(post("/api/validated").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}")
                        .with(httpBasic("treasury-operator", "operator-local")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("name must not be blank"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").doesNotExist());
    }

    @Test
    void unhandledExceptionIsNotConvertedIntoAStarterErrorEnvelope() {
        assertThatThrownBy(() -> mvc.perform(get("/api/unhandled")
                        .with(httpBasic("treasury-viewer", "viewer-local"))))
                .hasRootCauseInstanceOf(UnsupportedOperationException.class)
                .hasRootCauseMessage("Unexpected sample failure");
    }

    @Test
    void everyDefaultPublicPathIsAnonymousAndHasItsCurrentResponse() throws Exception {
        mvc.perform(get("/docs/info"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"service\":\"sample\"}"));
        mvc.perform(get("/v3/api-docs/"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/swagger-ui/"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/h2-console/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void trailingSlashMatchesForViewerAndAnonymousRequestsRemainUnauthorized() throws Exception {
        mvc.perform(get("/api/ping/").with(httpBasic("treasury-viewer", "viewer-local")))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"ok\"}"));
        assertUnauthorized(mvc.perform(get("/api/ping/")).andReturn());
    }

    @Test
    void trailingSlashPreservesErrorEnvelopeBehavior() throws Exception {
        mvc.perform(get("/api/status/").header("X-Correlation-Id", "trace-404-slash")
                        .with(httpBasic("treasury-viewer", "viewer-local")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Sample resource is missing"))
                .andExpect(jsonPath("$.correlationId").value("trace-404-slash"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").doesNotExist());
    }

    private void assertUnauthorized(MvcResult result) throws Exception {
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
        assertThat(result.getResponse().getHeader("WWW-Authenticate")).isEqualTo("Basic realm=\"Realm\"");
        assertThat(result.getResponse().getContentAsString()).isEmpty();
    }
}
