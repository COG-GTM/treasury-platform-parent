package com.harborline.treasury.platform;

import com.harborline.treasury.platform.security.TreasurySecurityConfiguration;
import com.harborline.treasury.platform.web.ApiErrorHandler;
import com.harborline.treasury.platform.web.CorrelationIdFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.web.filter.UrlHandlerFilter;

/**
 * Platform defaults every treasury service gets by depending on treasury-starter:
 * request correlation IDs, the shared API error envelope and baseline HTTP security.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "treasury.platform", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TreasuryPlatformProperties.class)
@Import({ApiErrorHandler.class, TreasurySecurityConfiguration.class})
public class TreasuryPlatformAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdFilter correlationIdFilter(TreasuryPlatformProperties properties) {
        return new CorrelationIdFilter(properties.getCorrelationHeader());
    }

    @Bean
    public FilterRegistrationBean<UrlHandlerFilter> treasuryTrailingSlashFilter() {
        FilterRegistrationBean<UrlHandlerFilter> registration =
                new FilterRegistrationBean<>(UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build());
        registration.setName("treasuryTrailingSlashFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}
