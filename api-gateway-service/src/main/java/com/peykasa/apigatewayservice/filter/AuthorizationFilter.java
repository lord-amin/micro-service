package com.peykasa.apigatewayservice.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peykasa.apigatewayservice.service.AuthenticationService;
import com.peykasa.apigatewayservice.RestErrorResponse;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


/**
 * @author Kamran Ghiasvand
 */
@Component
@ConditionalOnProperty(value = "app.filter.security.enabled", havingValue = "true", matchIfMissing = true)
public class AuthorizationFilter implements GlobalFilter, Ordered {
    private static final String UNAUTHORIZED = "unauthorized";
    private static final String AUTHENTICATION_REQUIRED_MSG = "Full authentication is required to access this resource";
    private static final String AUTHENTICATION_USER_KEY = "authentication_user";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);
    private final SecConfig appConfig;
    private final ObjectMapper mapper;
    private final AuthenticationService authenticationService;

    @Autowired
    public AuthorizationFilter(AuthenticationService authenticationService, ObjectMapper mapper, SecConfig appConfig) {
        this.authenticationService = authenticationService;
        this.mapper = mapper;
        this.appConfig = appConfig;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        if (HttpMethod.OPTIONS.equals(req.getMethod())) {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        }
        try {

            OAuthUser anonymous = new OAuthUser();
            anonymous.setUsername("anonymous");
            anonymous.setClient(new OAuthUser.Client());
            anonymous.getClient().setClientId("anonymous");

            final String requestURI = req.getURI().toString();
            for (Pattern pattern : appConfig.getExcludePathPatterns()) {
                LOGGER.info("Checking received path '{}' with config '{}' ...", requestURI, pattern.pattern());
                if (pattern.matcher(requestURI).matches()) {
                    LOGGER.warn("Matched pattern is '{}', so excluded", pattern.pattern());
                    anonymous.setSuperAdmin(true);
                    ServerHttpRequest build = req.mutate().header(AUTHENTICATION_USER_KEY, mapper.writeValueAsString(anonymous)).build();
                    return chain.filter(exchange.mutate().request(build).build());
                }
            }
            List<String> authorization = req.getHeaders().get("Authorization");

            if (authorization == null || authorization.isEmpty() || "".equals(authorization.get(0))) {
                ServerHttpResponse response = exchange.getResponse();
                return error(response, HttpURLConnection.HTTP_UNAUTHORIZED, UNAUTHORIZED, AUTHENTICATION_REQUIRED_MSG);
            }
            LOGGER.info("Token : " + authorization);

            HashMap<String, String> body = new HashMap<String, String>() {{
                put("method", req.getMethod().name());
                put("url", requestURI);
            }};

            try {
                LOGGER.info("Calling url '{}:{}'", req.getMethod(), requestURI);
                OAuthUser oAuthUser = authenticationService.checkAccess(body, authorization.get(0));
                if (oAuthUser == null) {
                    return error(exchange.getResponse(), HttpURLConnection.HTTP_UNAUTHORIZED, UNAUTHORIZED, "User not found");
                }
                ServerHttpRequest build = req.mutate().header(AUTHENTICATION_USER_KEY, mapper.writeValueAsString(oAuthUser)).build();
                return chain.filter(exchange.mutate().request(build).build());
            } catch (FeignException c) {
                LOGGER.error("", c);
                Optional<ByteBuffer> byteBuffer = c.responseBody();
                if (byteBuffer.isPresent()) {
                    return error(exchange.getResponse(), c.status(), byteBuffer.get().array());
                }
                if (c.status() == 401) {
                    return error(exchange.getResponse(), c.status(), UNAUTHORIZED, "");
                } else
                    return error(exchange.getResponse(), c.status(), "client_error", c.getMessage());
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
                return error(exchange.getResponse(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "unhandled_error", ex.getMessage());

            }
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        return chain.filter(exchange);
    }

    public Mono<Void> error(ServerHttpResponse response, int status, String key, String msg) throws IOException {
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        byte[] obj;
        try {
            obj = mapper.writeValueAsBytes(new RestErrorResponse(key, msg));
            response.setStatusCode(HttpStatus.resolve(status));
            return response.writeWith(Mono.just(obj).map(dataBufferFactory::wrap));
        } catch (JsonProcessingException e) {
            LOGGER.error("", e);
        }
        return response.setComplete();
    }

    public Mono<Void> error(ServerHttpResponse response, int status, byte[] body) {
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        response.setStatusCode(HttpStatus.resolve(status));
        return response.writeWith(Mono.just(body).map(dataBufferFactory::wrap));
    }

    @Override
    public int getOrder() {
        return -1000;
    }
}
