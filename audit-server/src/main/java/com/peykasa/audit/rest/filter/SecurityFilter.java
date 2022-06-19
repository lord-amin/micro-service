//package com.peykasa.audit.rest.filter;
//
//
//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.peykasa.audit.common.config.AppConfig;
//import com.peykasa.audit.common.oauth.OAuthUser;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.net.HttpURLConnection;
//import java.util.HashMap;
//import java.util.regex.Pattern;
//
///**
// * @author kamran
// */
//@Component
//public class SecurityFilter implements Filter {
//    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter.class);
//    public static final String AUTHENTICATION_USER_KEY = "authenticationUser";
//    private final AppConfig appConfig;
//    //    private final PathPermissionMapper pathPermissionMapper;
//    private ObjectMapper mapper = new ObjectMapper();
//
//    @Autowired
//    public SecurityFilter(AppConfig appConfig) {
//        this.appConfig = appConfig;
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY)
//                .withGetterVisibility(JsonAutoDetect.Visibility.NONE));
//    }
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//        LOGGER.info("Init ...");
//    }
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        HttpServletResponse res = (HttpServletResponse) servletResponse;
//        HttpServletRequest req = (HttpServletRequest) servletRequest;
//        LOGGER.info("Request from {} ", req.getRemoteAddr() + ":" + req.getRemotePort() + ":" + req.getLocalPort() + ":" + req.getServerPort() + req.getRequestURI());
//
//        if (req.getMethod().equals(RequestMethod.OPTIONS.name())) {
//            filterChain.doFilter(servletRequest, servletResponse);
//            return;
//        }
//        for (Pattern pattern : appConfig.getExcludePathPatterns()) {
//            if (pattern.matcher(req.getRequestURI()).matches()) {
//                LOGGER.warn("Exclude path {} from security ...", req.getRequestURI());
//                filterChain.doFilter(servletRequest, servletResponse);
//                return;
//            }
//        }
//        if (!appConfig.isSecurityEnable()) {
//            LOGGER.warn("The security checking disabled ...");
//            OAuthUser anonymous = new OAuthUser();
//            anonymous.setUsername("anonymous");
//            anonymous.setId(-1L);
//            req.setAttribute(AUTHENTICATION_USER_KEY, new OAuthUser());
//        } else {
//            String authorization = req.getHeader("Authorization");
//            if (authorization == null || "".equals(authorization)) {
//                LOGGER.warn(HttpURLConnection.HTTP_UNAUTHORIZED + "full authentication is required to access this resource");
//                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "full authentication is required to access this resource");
//                return;
//            }
//            LOGGER.info("Token : " + authorization);
//            String url = appConfig.getAuthServerContextPath() + appConfig.getAuthServerProfilePath();
//
//            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", authorization);
//
//            HttpEntity<?> httpEntity = new HttpEntity<>(new HashMap<String, String>() {{
//                put("method", req.getMethod());
//                put("url", req.getRequestURI());
//            }}, headers);
//
//            try {
//                ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
//                if (exchange.getStatusCode() == HttpStatus.OK) {
//                    String body = exchange.getBody();
//                    OAuthUser user = mapper.readValue(body, OAuthUser.class);
//                    if (user == null) {
//                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No user found");
//                        return;
//                    }
//                    LOGGER.info("profile= {}", user.toString());
//                    req.setAttribute(AUTHENTICATION_USER_KEY, user);
//                } else {
//                    if (exchange.getStatusCode() == HttpStatus.FORBIDDEN) {
//                        res.sendError(HttpServletResponse.SC_FORBIDDEN, "permission denied");
//                        return;
//                    } else {
//                        res.sendError(HttpServletResponse.SC_FORBIDDEN, exchange.getBody());
//                        return;
//                    }
//                }
//            } catch (HttpClientErrorException c) {
//                if (c.getStatusCode() == HttpStatus.FORBIDDEN) {
//                    res.sendError(HttpServletResponse.SC_FORBIDDEN, "permission denied");
//                    return;
//                } else {
//                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, c.getResponseBodyAsString());
//                    return;
//                }
//            } catch (Exception ex) {
//                LOGGER.error(ex.getMessage(), ex);
//                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
//                return;
//            }
//        }
//        filterChain.doFilter(servletRequest, servletResponse);
//    }
//
//    @Override
//    public void destroy() {
//        LOGGER.info("Destroy...");
//    }
//}