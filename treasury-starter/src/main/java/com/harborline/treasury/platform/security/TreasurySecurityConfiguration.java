package com.harborline.treasury.platform.security;

import com.harborline.treasury.platform.TreasuryPlatformProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Baseline API security: read endpoints need TREASURY_VIEWER, anything that moves money
 * or changes state needs TREASURY_OPERATOR. Services can extend the public path list.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class TreasurySecurityConfiguration extends WebSecurityConfigurerAdapter {

    public static final String VIEWER = "TREASURY_VIEWER";
    public static final String OPERATOR = "TREASURY_OPERATOR";

    private final TreasuryPlatformProperties properties;

    public TreasurySecurityConfiguration(TreasuryPlatformProperties properties) {
        this.properties = properties;
    }

    @Bean
    public PasswordEncoder treasuryPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        TreasuryPlatformProperties.Security security = properties.getSecurity();
        PasswordEncoder encoder = treasuryPasswordEncoder();
        auth.inMemoryAuthentication()
                .withUser(security.getOperatorUser()).password(encoder.encode(security.getOperatorPassword()))
                .roles(OPERATOR, VIEWER)
                .and()
                .withUser(security.getViewerUser()).password(encoder.encode(security.getViewerPassword()))
                .roles(VIEWER);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .headers().frameOptions().sameOrigin()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(properties.getSecurity().getPublicPaths().toArray(new String[0])).permitAll()
                .antMatchers(HttpMethod.GET, "/api/**").hasRole(VIEWER)
                .antMatchers("/api/**").hasRole(OPERATOR)
                .anyRequest().authenticated()
                .and()
                .httpBasic();
    }
}
