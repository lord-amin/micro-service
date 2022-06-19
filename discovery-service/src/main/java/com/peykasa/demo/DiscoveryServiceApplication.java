package com.peykasa.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryServiceApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DiscoveryServiceApplication.class, args);
        logConfig(context);
        ConfigurableEnvironment environment = context.getEnvironment();
        String property = environment.getProperty("server.address");
        String property1 = environment.getProperty("server.servlet.contextPath");
        String base = "http://" + (property == null ? "localhost" : property) + ":" + environment.getProperty("server.port") + (property1 == null ? "" : property1);
        String json = base + environment.getProperty("springfox.documentation.swagger.v2.path");
        String ui = base + "/swagger-ui/index.html";
        LOGGER.info("resource json address is [" + json + "]");
        LOGGER.info("resource ui address is [" + ui + "]");
    }

    private static void logConfig(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource instanceof MapPropertySource) {
                MapPropertySource propertySource1 = (MapPropertySource) propertySource;
                for (String propertyName : propertySource1.getPropertyNames()) {
                    LOGGER.info("       {}={}", propertyName, environment.getProperty(propertyName));
                }
            }
        }
    }
}
