package com.peykasa.authserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
public class HttpUtil {
    @Autowired
    private TestRestTemplate testRestTemplate;
    private static final String loginUrl = "/oauth/token";

    public String login(String u, String p) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getBasicHeader("portal-client-id", "portal-client-secret"));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("username", u);
        params.add("password", p);
        params.add("grant_type", "password");
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(params, headers);
        ResponseEntity<OAuth2AccessToken> exchange = testRestTemplate.exchange(URI.create(loginUrl), HttpMethod.POST, requestEntity, OAuth2AccessToken.class);
        return exchange.getBody().getValue();
    }

    public <E, R> ResponseEntity<R> post(String u, String p, String url, E req, Class<R> tClass) {
        String token = login(u, p);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> requestEntity =
                new HttpEntity<>(req, headers);
        return testRestTemplate.exchange(URI.create(url), HttpMethod.POST, requestEntity, tClass);
    }

    public <E, R> ResponseEntity<R> post(String u, String p, String url, String produce, E req, Class<R> tClass) {
        String token = login(u, p);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.ACCEPT, produce);
        HttpEntity<?> requestEntity =
                new HttpEntity<>(req, headers);
        return testRestTemplate.exchange(URI.create(url), HttpMethod.POST, requestEntity, tClass);
    }

    public <E, R> ResponseEntity<R> patch(String u, String p, String url, E req, Class<R> tClass) {
        String token = login(u, p);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> requestEntity =
                new HttpEntity<>(req, headers);
        return testRestTemplate.exchange(URI.create(url), HttpMethod.PATCH, requestEntity, tClass);
    }

    public <T> ResponseEntity<T> get(String u, String p, String url, Map<String, Object> params, Class<T> tClass) {
        String token = login(u, p);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return testRestTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                tClass);
    }

    public <T> ResponseEntity<T> delete(String u, String p, String url, Class<T> tClass) {
        String token = login(u, p);
        System.out.println("token is " + token);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return testRestTemplate.exchange(
                builder.toUriString(),
                HttpMethod.DELETE,
                entity,
                tClass);
    }

    public <T> ResponseEntity<T> send(String token, String url, Class<T> tClass) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return testRestTemplate.exchange(
                builder.toUriString(),
                HttpMethod.DELETE,
                entity,
                tClass);
    }

    private String getBasicHeader(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(
                auth.getBytes(Charset.forName("US-ASCII")));
        return "Basic " + new String(encodedAuth);
    }


}
