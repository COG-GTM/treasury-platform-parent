package com.harborline.treasury.platform;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "treasury.platform")
public class TreasuryPlatformProperties {

    private final boolean enabled;
    private final String correlationHeader;
    private final Security security;

    public TreasuryPlatformProperties(@DefaultValue("true") boolean enabled,
                                      @DefaultValue("X-Correlation-Id") String correlationHeader,
                                      @DefaultValue Security security) {
        this.enabled = enabled;
        this.correlationHeader = correlationHeader;
        this.security = security;
    }

    public boolean isEnabled() { return enabled; }
    public String getCorrelationHeader() { return correlationHeader; }
    public Security getSecurity() { return security; }

    public static class Security {
        private final List<String> publicPaths;
        private final String operatorUser;
        private final String operatorPassword;
        private final String viewerUser;
        private final String viewerPassword;

        public Security(@DefaultValue({"/docs/**", "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/h2-console/**"}) List<String> publicPaths,
                        @DefaultValue("treasury-operator") String operatorUser,
                        @DefaultValue("operator-local") String operatorPassword,
                        @DefaultValue("treasury-viewer") String viewerUser,
                        @DefaultValue("viewer-local") String viewerPassword) {
            this.publicPaths = publicPaths;
            this.operatorUser = operatorUser;
            this.operatorPassword = operatorPassword;
            this.viewerUser = viewerUser;
            this.viewerPassword = viewerPassword;
        }

        public List<String> getPublicPaths() { return publicPaths; }
        public String getOperatorUser() { return operatorUser; }
        public String getOperatorPassword() { return operatorPassword; }
        public String getViewerUser() { return viewerUser; }
        public String getViewerPassword() { return viewerPassword; }
    }
}
