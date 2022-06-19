package com.peykasa.silo.central.service;

import com.peykasa.silo.central.filter.OAuthUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

/**
 * @author Kamran Ghiasvand
 */
@FeignClient("authentication-service")
public interface AuthenticationService {
    @PostMapping(path = "/api/authorize")
    OAuthUser checkAccess(Map<String,String> body, @RequestHeader("Authorization") String token);

    @GetMapping(path = "/api/me")
    UserEntity getProfile(@RequestHeader("Authorization") String token);
}
