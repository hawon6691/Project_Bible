package com.pbshop.springshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.pbshop.springshop.common.web.ApiRequestContextFilter;

@Configuration
public class SecurityConfig {

    private final SecurityProblemSupport securityProblemSupport;
    private final ApiRequestContextFilter apiRequestContextFilter;

    public SecurityConfig(SecurityProblemSupport securityProblemSupport, ApiRequestContextFilter apiRequestContextFilter) {
        this.securityProblemSupport = securityProblemSupport;
        this.apiRequestContextFilter = apiRequestContextFilter;
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
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/api/v1/health",
                    "/actuator/health",
                    "/actuator/info",
                    "/actuator/prometheus"
                )
                .permitAll()
                .anyRequest()
                .authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
