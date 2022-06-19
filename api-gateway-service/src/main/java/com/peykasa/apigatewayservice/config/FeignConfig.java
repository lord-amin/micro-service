//package com.peykasa.silo.central.apigatewayservice.config;
//
//import feign.RequestInterceptor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
//
///**
// * @author Kamran Ghiasvand
// */
//@Configuration
//public class FeignConfig {
//    @Bean
//    public RequestInterceptor requestTokenBearerInterceptor() {
//        return template -> {
//            var details =
//                    (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
//
//            final var token = details.getTokenValue();
//            if (token != null)
//                template.header("Authorization", "bearer " + token);
//        };
//    }
//}
