package com.peykasa.apigatewayservice.service;

import com.peykasa.apigatewayservice.filter.OAuthUser;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * @author Kamran Ghiasvand
 */
@FeignClient("authentication-service")
@Import(FeignAutoConfiguration.class)
public interface AuthenticationService {
    @PostMapping(path = "/api/authorize",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    OAuthUser checkAccess(Map<String, String> body, @RequestHeader("Authorization") String token);

    @GetMapping(path = "/authentication-service/v1/api/profile")
    UserEntity getProfile();
}
