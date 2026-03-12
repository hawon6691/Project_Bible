package com.pbshop.springshop.config;

import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.pbshop.springshop.auth.security.BearerTokenAuthenticationFilter;
import com.pbshop.springshop.common.web.ApiRequestContextFilter;

@Configuration
public class SecurityConfig {

    private final SecurityProblemSupport securityProblemSupport;
    private final ApiRequestContextFilter apiRequestContextFilter;
    private final BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter;

    public SecurityConfig(
            SecurityProblemSupport securityProblemSupport,
            ApiRequestContextFilter apiRequestContextFilter,
            BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter
    ) {
        this.securityProblemSupport = securityProblemSupport;
        this.apiRequestContextFilter = apiRequestContextFilter;
        this.bearerTokenAuthenticationFilter = bearerTokenAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(securityProblemSupport)
                .accessDeniedHandler(securityProblemSupport)
            )
            .addFilterBefore(apiRequestContextFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(bearerTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/api/v1/health",
                    "/actuator/health",
                    "/actuator/info",
                    "/actuator/prometheus"
                )
                .permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/auth/signup",
                    "/api/v1/auth/verify-email",
                    "/api/v1/auth/resend-verification",
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/password-reset/request",
                    "/api/v1/auth/password-reset/verify",
                    "/api/v1/auth/password-reset/confirm"
                )
                .permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/categories",
                    "/api/v1/categories/*",
                    "/api/v1/deals",
                    "/api/v1/deals/*",
                    "/api/v1/products/*/real-price",
                    "/api/v1/fraud/products/*/effective-prices",
                    "/api/v1/fraud/products/*/anomalies",
                    "/api/v1/trust/sellers/*",
                    "/api/v1/trust/sellers/*/history",
                    "/api/v1/i18n/translations",
                    "/api/v1/i18n/exchange-rates",
                    "/api/v1/i18n/convert",
                    "/api/v1/images/*/variants",
                    "/api/v1/badges",
                    "/api/v1/predictions/products/*/price-trend",
                    "/api/v1/products",
                    "/api/v1/products/*",
                    "/api/v1/products/*/inquiries",
                    "/api/v1/products/*/reviews",
                    "/api/v1/products/*/specs",
                    "/api/v1/products/*/prices",
                    "/api/v1/products/*/price-history",
                    "/api/v1/rankings/products/popular",
                    "/api/v1/rankings/keywords/popular",
                    "/api/v1/recommendations/trending",
                    "/api/v1/boards",
                    "/api/v1/boards/*/posts",
                    "/api/v1/posts/*",
                    "/api/v1/posts/*/comments",
                    "/api/v1/specs/definitions",
                    "/api/v1/sellers",
                    "/api/v1/sellers/*",
                    "/api/v1/users/*/badges",
                    "/api/v1/users/*/profile",
                    "/api/v1/auth/login/*",
                    "/api/v1/auth/callback/*"
                )
                .permitAll()
                .anyRequest()
                .authenticated()
            )
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
