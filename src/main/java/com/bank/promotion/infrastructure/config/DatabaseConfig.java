package com.bank.promotion.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.bank.promotion.infrastructure.repository")
@EntityScan(basePackages = "com.bank.promotion.infrastructure.entity")
@EnableTransactionManagement
public class DatabaseConfig {
}