package com.peykasa.authserver.config;

import com.peykasa.authserver.service.CustomAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author Taher Khorshidi, Yaser(amin) Sadeghi
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${admin.reset.password.url:}")
    private String adminResetPassUrl;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, CustomAuthenticationProvider authenticationProvider, AuthenticationEventPublisher eventPublisher) {
        this.userDetailsService = userDetailsService;
        this.authenticationProvider = authenticationProvider;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        if (!"".equals(adminResetPassUrl))
            web.ignoring().antMatchers(adminResetPassUrl);
        web.ignoring().antMatchers("/api/profile**");
        web.ignoring().antMatchers("/api/applications**");
        web.ignoring().antMatchers("/browser/**");
        web.ignoring().antMatchers("/monitoring*/**");
        web.ignoring().antMatchers("/admin/**");
        web.ignoring().antMatchers("/loggers/**");
        web.ignoring().antMatchers("/**/*swagger*/**");
        web.ignoring().antMatchers("/api-docs*");
        web.ignoring().antMatchers("/**/*.js");
        web.ignoring().antMatchers("/**/*.css");

    }

    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationProvider authenticationProvider;
    private final AuthenticationEventPublisher eventPublisher;

    //
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationEventPublisher(eventPublisher);
        auth.userDetailsService(userDetailsService);
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        System.out.println(http);
    }
}
