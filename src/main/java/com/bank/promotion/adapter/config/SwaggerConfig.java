package com.bank.promotion.adapter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 配置類別
 * 提供 API 文檔的基本配置和安全性設定
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(createServer()))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("銀行客戶優惠推薦系統 API")
                .description("基於決策樹的智能優惠推薦系統，提供客戶優惠評估、規則管理和稽核追蹤功能")
                .version("1.0.0")
                .contact(new Contact()
                        .name("開發團隊")
                        .email("dev-team@bank.com")
                        .url("https://bank.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private Server createServer() {
        String serverUrl = getServerUrl();
        return new Server()
                .url(serverUrl)
                .description(getServerDescription());
    }

    private String getServerUrl() {
        String baseUrl = switch (activeProfile) {
            case "prod" -> "https://api.bank.com";
            case "uat" -> "https://uat-api.bank.com";
            case "sit" -> "https://sit-api.bank.com";
            default -> "http://localhost:8080";
        };
        return baseUrl + contextPath;
    }

    private String getServerDescription() {
        return switch (activeProfile) {
            case "prod" -> "生產環境";
            case "uat" -> "使用者驗收測試環境";
            case "sit" -> "系統整合測試環境";
            default -> "開發環境";
        };
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("請在此輸入 JWT Token (格式: Bearer {token})");
    }
}