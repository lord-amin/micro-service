package com.peykasa.authserver.config;

import com.google.common.base.Predicates;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Yaser(amin) Sadeghi
 */
@Configuration
@EnableSwagger2
@EnableAutoConfiguration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(requestHandler -> {
                    Set<RequestMethod> requestMethods = requestHandler.supportedMethods();
                    Set<String> patterns = requestHandler.getPatternsCondition().getPatterns();
                    for (String pattern : patterns) {
                        if (pattern.startsWith("/oauth/confirm_access"))
                            return false;
//                        if (get("/api/clients", requestMethods, pattern))
//                            return false;
                        if (get("/api/permissions", requestMethods, pattern))
                            return false;
                        if (pattern.startsWith("/api/permissions/{id}"))
                            return false;
                    }
                    return true;
                })
                .paths(PathSelectors.regex(".*"))
                .paths(Predicates.not(PathSelectors.regex("/api/applications.*")))
                .paths(Predicates.not(PathSelectors.regex("/monitoring.*")))
                .paths(Predicates.not(PathSelectors.regex("/admin.*")))
                .paths(Predicates.not(PathSelectors.regex("/oauth/[^t|^c].*")))
                .paths(Predicates.not(PathSelectors.regex("/error.*")))
                .paths(Predicates.not(PathSelectors.regex("/api/journal.*")))
                .paths(Predicates.not(PathSelectors.regex(".*profile.*")))
                .paths(Predicates.not(PathSelectors.regex("/api/deleted.*")))
                .build().apiInfo(getApiInfo()).securitySchemes(Collections.singletonList(new ApiKey("Authorization", "Authorization", "header")))
                .securityContexts(Collections.singletonList(securityContext()));
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.regex("/api.*")).build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessNothing");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(new SecurityReference("Authorization", authorizationScopes));
    }

    private boolean get(String path, Set<RequestMethod> requestMethods, String pattern) {
        if (pattern.startsWith(path)) {
            boolean get = false;
            for (RequestMethod requestMethod : requestMethods) {
                String name = requestMethod.name();
                if ("GET".equals(name))
                    get = true;
            }
            if (!get)
                return true;
        }
        return false;
    }

    private boolean client(String path, Set<RequestMethod> requestMethods, String pattern) {
        if (pattern.startsWith(path)) {
            boolean get = false;
            for (RequestMethod requestMethod : requestMethods) {
                String name = requestMethod.name();
                if ("GET".equals(name))
                    get = true;
            }
            if (!get)
                return true;
        }
        return false;
    }

    public ApiInfo getApiInfo() {
        return new ApiInfo(
                "PA Auth Server",
                "Peykasa Authentication and Authorization Server",
                "",
                "",
                null,
                "",
                null,
                Collections.emptyList()
        );
    }
}