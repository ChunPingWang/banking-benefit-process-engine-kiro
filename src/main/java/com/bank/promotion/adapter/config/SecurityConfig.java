package com.bank.promotion.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

/**
 * 安全配置
 * 提供基本的API安全性和權限控制
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/promotions/evaluate").permitAll()
                .requestMatchers("/api/v1/promotions/history/**").authenticated()
                .requestMatchers("/api/v1/promotions/available/**").authenticated()
                .requestMatchers("/api/v1/management/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/v1/audit/**").hasAnyRole("ADMIN", "AUDITOR")
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {});
        
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .authorities(Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_MANAGER"),
                new SimpleGrantedAuthority("ROLE_AUDITOR")
            ))
            .build();
        
        UserDetails manager = User.builder()
            .username("manager")
            .password(passwordEncoder().encode("manager123"))
            .authorities(Arrays.asList(
                new SimpleGrantedAuthority("ROLE_MANAGER")
            ))
            .build();
        
        UserDetails auditor = User.builder()
            .username("auditor")
            .password(passwordEncoder().encode("auditor123"))
            .authorities(Arrays.asList(
                new SimpleGrantedAuthority("ROLE_AUDITOR")
            ))
            .build();
        
        return new InMemoryUserDetailsManager(admin, manager, auditor);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}