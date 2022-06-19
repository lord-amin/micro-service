package com.peykasa.audit.common.config;


import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
public class AppConfig {
//    @Value("${rs.security.enable:true}")
//    private boolean securityEnable;
//    @Value("${rs.security.auth.server.context.path}")
//    private String authServerContextPath;
//    @Value("${rs.security.auth.server.profile.path:/api/authorize}")
//    private String authServerProfilePath;
//    @Value("${rs.security.ignore.expressions:}")
//    private String securityIgnoreExpressions;

//    private ElasticConfig elasticConfig;


    //    public boolean isSecurityEnable() {
//        return securityEnable;
//    }
//
//    public String getAuthServerContextPath() {
//        return authServerContextPath;
//    }
//
//    public String getAuthServerProfilePath() {
//        return authServerProfilePath;
//    }
//
//    public List<Pattern> getExcludePathPatterns() {
//        List<Pattern> patterns = new ArrayList<>();
//        if (!securityIgnoreExpressions.isEmpty()) {
//            String[] split = securityIgnoreExpressions.trim().split("^\\[|\\]$|\\[\\s*\\]|\\]\\s*\\[");
//            for (String s : split) {
//                if (s.isEmpty())
//                    continue;
//                try {
//                    patterns.add(Pattern.compile(s.trim()));
//                } catch (PatternSyntaxException e) {
//                    throw new IllegalArgumentException("Invalid regex='" + s + "' in security ignore ", e);
//                }
//            }
//        }
//        return patterns;
//    }
//    @Autowired
//    public AppConfig(ElasticConfig elasticConfig) {
//        this.elasticConfig = elasticConfig;
//    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        JettyServletWebServerFactory factory = new JettyServletWebServerFactory();
        Collection<JettyServerCustomizer> serverCustomizers = factory.getServerCustomizers();
        serverCustomizers.add(server -> {
            Connector[] connectors = server.getConnectors();
            for (Connector connector : connectors) {
                ((ServerConnector) connector).setIdleTimeout(60000);
            }

        });
        return factory;
    }


}
