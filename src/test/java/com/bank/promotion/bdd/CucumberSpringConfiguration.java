package com.bank.promotion.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Cucumber Spring Boot 整合配置
 * 提供測試環境的 Spring 上下文
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({BddTestConfiguration.class, TestSecurityConfiguration.class})
public class CucumberSpringConfiguration {
}