package com.peykasa.silo.central.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.peykasa.silo.central.RestErrorResponse;
import com.peykasa.silo.central.service.AuthenticationService;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.regex.Pattern;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * @author Kamran Ghiasvand
 */
@Component
@ConditionalOnProperty(value = "app.filter.security.enabled", havingValue = "true", matchIfMissing = true)
public class AuthorizationFilter extends ZuulFilter {
    private static final String UNAUTHORIZED = "unauthorized";
    private static final String AUTHENTICATION_REQUIRED_MSG = "Full authentication is required to access this resource";
    private static final String AUTHENTICATION_USER_KEY = "authentication_user";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);
    private final SecConfig appConfig;
    private ObjectMapper mapper = new ObjectMapper();
    private final AuthenticationService authenticationService;


    public AuthorizationFilter(SecConfig appConfig, AuthenticationService authenticationService) {
        this.appConfig = appConfig;
        this.authenticationService = authenticationService;
    }

    @Override
    public Object run() {
        RequestContext.getCurrentContext().setDebugRequest(true);
        handle();
        return null;
    }

    private boolean handle() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest req = context.getRequest();
        if (req.getMethod().equals(RequestMethod.OPTIONS.name())) {
            RequestContext.getCurrentContext().setSendZuulResponse(false);
            return false;
        }
        try {

            OAuthUser anonymous = new OAuthUser();
            anonymous.setUsername("anonymous");
            anonymous.setClient(new OAuthUser.Client());
            anonymous.getClient().setClientId("anonymous");

            final String requestURI = req.getRequestURI();
            for (Pattern pattern : appConfig.getExcludePathPatterns()) {
                LOGGER.info("Checking received path '{}' with config '{}' ...", requestURI, pattern.pattern());
                if (pattern.matcher(requestURI).matches()) {
                    LOGGER.warn("Matched pattern is '{}', so excluded", pattern.pattern());
                    anonymous.setSuperAdmin(true);
                    context.addZuulRequestHeader(AUTHENTICATION_USER_KEY, mapper.writeValueAsString(anonymous));
                    context.setSendZuulResponse(true);
                    return false;
                }
            }
            String authorization = req.getHeader("Authorization");
            if (authorization == null || "".equals(authorization)) {
                error(HttpURLConnection.HTTP_UNAUTHORIZED, UNAUTHORIZED, AUTHENTICATION_REQUIRED_MSG);
                return true;
            }
            LOGGER.info("Token : " + authorization);

            HashMap<String, String> body = new HashMap<String, String>() {{
                put("method", req.getMethod());
                put("url", requestURI);
            }};

            try {
                LOGGER.info("Calling url '{}:{}'", req.getMethod(), requestURI);
                OAuthUser oAuthUser = authenticationService.checkAccess(body, authorization);
//                if (exchange.getStatusCode() == HttpStatus.OK) {
//                    String body = exchange.getBody();
//                    OAuthUser user = mapper.readValue(body, OAuthUser.class);
                if (oAuthUser == null) {
                    error(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED, "User not found");
                    return true;
                }
//                    LOGGER.info("profile= {}", user);
//                    String value = mapper.writeValueAsString(user);
//                    LOGGER.info("profile>>= {}", value);
                context.addZuulRequestHeader(AUTHENTICATION_USER_KEY, mapper.writeValueAsString(oAuthUser));
//                } else {
//                    error(exchange.getStatusCode().value(), exchange.getBody());
//                }
            } catch (FeignException c) {
                LOGGER.error("", c);
                byte[] array = c.responseBody().get().array();
                String resp = new String(array, Charset.forName("UTF-8"));
                if (c.status() == 401) {
                    error(c.status(), UNAUTHORIZED, "");
                } else
                    error(c.status(), "client_error", c.getMessage());
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
                error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "unhandled_error", ex.getMessage());

            }
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        return true;
    }

    public void error(int status, String key, String msg) throws IOException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletResponse response = ctx.getResponse();
        response.setCharacterEncoding("UTF-8");
        OutputStream outStream = response.getOutputStream();
        InputStream is = new ByteArrayInputStream(mapper.writeValueAsBytes(new RestErrorResponse(key, msg)));
        try {
            writeResponse(is, outStream);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        RequestContext.getCurrentContext().setResponseStatusCode(status);
        RequestContext.getCurrentContext().setSendZuulResponse(false);
    }

    private void writeResponse(InputStream zin, OutputStream out) throws Exception {
        byte[] bytes = new byte[1024];
        int bytesRead = -1;
        while ((bytesRead = zin.read(bytes)) != -1) {
            out.write(bytes, 0, bytesRead);
        }
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 2;
    }
}
