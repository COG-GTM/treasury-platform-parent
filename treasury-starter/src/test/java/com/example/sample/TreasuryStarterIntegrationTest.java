package com.example.sample;

import com.harborline.treasury.platform.web.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void starterIsDiscoveredThroughAutoConfiguration() {
        assertThat(context.getBeansOfType(CorrelationIdFilter.class)).hasSize(1);
    }

    @Test
    void apiRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/ping")).andExpect(status().isUnauthorized());
    }

    @Test
    void viewerCanReadAndReceivesCorrelationId() throws Exception {
        mvc.perform(get("/api/ping").with(httpBasic("treasury-viewer", "viewer-local")))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
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
                .andExpect(status().isForbidden());
    }

    @Test
    void operatorCanChangeState() throws Exception {
        mvc.perform(post("/api/transfers").with(httpBasic("treasury-operator", "operator-local")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("accepted"));
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
    void publicPathsAreOpen() throws Exception {
        mvc.perform(get("/docs/info")).andExpect(status().isOk());
    }

    @Test
    void springdocEntryPointIsPublic() throws Exception {
        // springdoc serves its UI entry at /swagger-ui.html; it must not be challenged for credentials.
        mvc.perform(get("/swagger-ui.html")).andExpect(status().isNotFound());
    }
}
