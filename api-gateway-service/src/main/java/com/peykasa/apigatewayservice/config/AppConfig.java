package com.peykasa.apigatewayservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kamran Ghiasvand
 */
@ConfigurationProperties(prefix = "application")
@Configuration
@Data
public class AppConfig {
    private String userProfileHeader = "user_info";
}
