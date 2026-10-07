package com.example.sample;

import com.harborline.treasury.platform.TreasuryPlatformAutoConfiguration;
import com.harborline.treasury.platform.TreasuryPlatformProperties;
import com.harborline.treasury.platform.web.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class TreasuryPlatformPropertiesTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebMvcAutoConfiguration.class, SecurityAutoConfiguration.class,
                    TreasuryPlatformAutoConfiguration.class));

    @Test
    void defaultsApply() {
        runner.run(context -> {
            TreasuryPlatformProperties properties = context.getBean(TreasuryPlatformProperties.class);
            assertThat(properties.getCorrelationHeader()).isEqualTo("X-Correlation-Id");
            assertThat(properties.getSecurity().getPublicPaths()).contains("/docs/**");
        });
    }

    @Test
    void correlationHeaderIsConfigurable() {
        runner.withPropertyValues("treasury.platform.correlation-header=X-Request-Id")
                .run(context -> assertThat(context.getBean(CorrelationIdFilter.class).getHeaderName()).isEqualTo("X-Request-Id"));
    }

    @Test
    void platformCanBeDisabled() {
        runner.withPropertyValues("treasury.platform.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(CorrelationIdFilter.class));
    }
}
