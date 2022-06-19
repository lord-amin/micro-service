package com.peykasa.authserver.config;

import com.peykasa.authserver.Constants;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.audit.auditor.Auditor;
import com.peykasa.authserver.exception.RestErrorResponse;
import com.peykasa.authserver.model.entity.Permission;
import com.peykasa.authserver.model.entity.PermissionURL;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.PermissionRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.DefaultOAuth2ExceptionRenderer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2ExceptionRenderer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.peykasa.authserver.Constants.AUTHORITY_SUPER_ADMIN;

/**
 * @author Taher Khorshidi, Yaser(amin) Sadeghi
 */
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServerConfig.class);
    private PermissionRepository permissionRepository;
    private Auditor auditor;
    private List<String> commonURLs = new ArrayList<>();

    {
        String logout = "/api/me/logout";
        commonURLs.add(logout);
        String meUrl = "/api/me";
        commonURLs.add(meUrl);
        String checkUrl = "/api/permission/check";
        commonURLs.add(checkUrl);
        String passUrl = "/api/me/change-password";
        commonURLs.add(passUrl);
    }

    @Value("${resource.security.enabled:true}")
    private boolean secEnabled;

    @Autowired
    public ResourceServerConfig(PermissionRepository permissionRepository, Auditor auditor) {
        this.permissionRepository = permissionRepository;
        this.auditor = auditor;
    }


    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("oauth2-resource").tokenStore(tokenStore());
    }

    @Bean
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }


    public List<Triple> check(User user, List<APIConfig> urls) {
        List<Triple> map = new ArrayList<>();
        for (APIConfig url : urls) {
            if (user == null) {
                map.add(new Triple(url.getUrl(), url.getMethod(), false));
                continue;
            }
            boolean b = user.hasPermission(url.getMethod(), url.getUrl());
            map.add(new Triple(url.getUrl(), url.getMethod(), b));
        }
        return map;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        security(http);
    }

    private void security(HttpSecurity http) throws Exception {
        if (!secEnabled) {
            http.authorizeRequests().anyRequest().permitAll();
        } else {
            http.anonymous().disable();
            Iterable<Permission> all = permissionRepository.findAll();
            Map<APIConfig, Set<String>> map = new HashMap<>();
            for (Permission permission : all) {
                for (PermissionURL permissionURL : permission.getUrls()) {
                    APIConfig apiConfig = new APIConfig(permissionURL.getMethod(), permissionURL.getUrl());
                    Set<String> permissions = map.computeIfAbsent(apiConfig, k -> new HashSet<>());
                    permissions.add(permission.getPermission());
                }
            }
            for (Map.Entry<APIConfig, Set<String>> resourceEntry : map.entrySet()) {
                APIConfig key = resourceEntry.getKey();
                List<String> authorities = new ArrayList<>(resourceEntry.getValue());
                authorities.add(AUTHORITY_SUPER_ADMIN);
                HttpMethod resolve = HttpMethod.resolve(key.getMethod().toUpperCase());
                if (resolve == null) {
                    LOGGER.error("Bad method name {} for url {}", key.getMethod(), key.getUrl());
                    System.exit(1);
                }
                LOGGER.info("Setting authorization for url='{}' to authority='{}'", key.toString(), authorities);
                http.authorizeRequests().antMatchers(resolve, key.getUrl()).hasAnyAuthority(authorities.toArray(new String[]{}));
            }
            for (String commonURL : commonURLs) {
                http.authorizeRequests().antMatchers(commonURL).permitAll();
            }
//            http.authorizeRequests().antMatchers(adminUrl).hasAuthority(serverSuperAdmin);
        }
        http.exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler() {
            //            private WebResponseExceptionTranslator exceptionTranslator = new DefaultWebResponseExceptionTranslator();
            private OAuth2ExceptionRenderer exceptionRenderer = new DefaultOAuth2ExceptionRenderer();
            private HandlerExceptionResolver handlerExceptionResolver = new DefaultHandlerExceptionResolver();

            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException authException) throws IOException, ServletException {
                try {
                    ResponseEntity<RestErrorResponse> result = new ResponseEntity<>(new RestErrorResponse(Constants.PERMISSION_DENIED_ERROR, authException.getMessage()), HttpStatus.FORBIDDEN);
                    exceptionRenderer.handleHttpEntityResponse(result, new ServletWebRequest(request, response));
                    response.flushBuffer();
                } catch (ServletException e) {
                    // Re-use some of the default Spring dispatcher behaviour - the exception came from the filter chain and
                    // not from an MVC handler so it won't be caught by the dispatcher (even if there is one)
                    if (handlerExceptionResolver.resolveException(request, response, this, e) == null) {
                        throw e;
                    }
                } catch (IOException | RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    // Wrap other Exceptions. These are not expected to happen
                    throw new RuntimeException(e);
                }
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof User) {
                    String requestURI = request.getRequestURI();
                    String context = Constants.USER;
                    if (requestURI.contains(Constants.ROLE_CONTEXT_PATH)) {
                        context = Constants.ROLE;
                    }

                    String event = "";
                    if (request.getMethod().equals(RequestMethod.POST.name())) {
                        event = Constants.CREATE;
                    } else if (request.getMethod().equals(RequestMethod.PUT.name())) {
                        event = Constants.UPDATE;
                    } else if (request.getMethod().equals(RequestMethod.PATCH.name())) {
                        event = Constants.UPDATE;
                    } else if (request.getMethod().equals(RequestMethod.DELETE.name())) {
                        event = Constants.DELETE;
                    } else if (request.getMethod().equals(RequestMethod.GET.name())) {
                        event = Constants.SEARCH;
                    }
                    if (requestURI.contains(Constants.USER_ROLES_PATH) || requestURI.contains(Constants.USER_DELETED_PATH) || requestURI.contains(Constants.ROLE_PERMISSIONS_PATH)) {
                        event = "";
                    }
                    if (requestURI.contains(Constants.USER_CHANGE_PASSWORD_PATH)) {
                        event = Constants.CHANGE_PASSWORD;
                    }

                    if ("".equals(event)) {
                        return;
                    }
                    Audit<?, AbstractMap.SimpleEntry> audit = new Audit<>(
                            new AbstractMap.SimpleEntry<>("Username", ((User) principal).getUsername()),
                            new AbstractMap.SimpleEntry<>("msg", authException.getMessage()));
                    audit.setEvent(event);
                    audit.setContext(context);
                    audit.setStatus(Constants.FAIL);
                    audit.setTime(new Date());
                    audit.setExtraInfo(requestURI);
                    audit.setRemoteAddress(request.getRemoteAddr());
                    auditor.audit(audit);
                }
            }
        });
    }

    @NoArgsConstructor
    @Data
    public class Triple {
        public Triple(String url, String method, boolean access) {
            this.url = url;
            this.method = method;
            this.access = access;
        }

        private String url;
        private String method;
        private boolean access;
    }
}
