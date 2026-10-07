package com.harborline.treasury.platform.security;

import com.harborline.treasury.platform.TreasuryPlatformProperties;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Baseline API security: read endpoints need TREASURY_VIEWER, anything that moves money
 * or changes state needs TREASURY_OPERATOR. Services can extend the public path list.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class TreasurySecurityConfiguration {

    public static final String VIEWER = "TREASURY_VIEWER";
    public static final String OPERATOR = "TREASURY_OPERATOR";

    @Bean
    public PasswordEncoder treasuryPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService treasuryUserDetailsService(TreasuryPlatformProperties properties, PasswordEncoder treasuryPasswordEncoder) {
        TreasuryPlatformProperties.Security security = properties.getSecurity();
        return new InMemoryUserDetailsManager(
                User.withUsername(security.getOperatorUser())
                        .password(treasuryPasswordEncoder.encode(security.getOperatorPassword()))
                        .roles(OPERATOR, VIEWER)
                        .build(),
                User.withUsername(security.getViewerUser())
                        .password(treasuryPasswordEncoder.encode(security.getViewerPassword()))
                        .roles(VIEWER)
                        .build());
    }

    @Bean
    public SecurityFilterChain treasurySecurityFilterChain(HttpSecurity http, TreasuryPlatformProperties properties) throws Exception {
        String[] publicPaths = properties.getSecurity().getPublicPaths().toArray(new String[0]);
        http.csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(publicPaths).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasRole(VIEWER)
                        .requestMatchers("/api/**").hasRole(OPERATOR)
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
