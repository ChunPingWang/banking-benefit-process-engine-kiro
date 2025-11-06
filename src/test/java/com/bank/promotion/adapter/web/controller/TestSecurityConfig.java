package com.bank.promotion.adapter.web.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class TestSecurityConfig {
    
    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/audit/**").hasAnyRole("ADMIN", "AUDITOR")
                .requestMatchers("/api/v1/management/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/query/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/promotions/evaluate").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/promotions/available").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {});
        
        return http.build();
    }
}