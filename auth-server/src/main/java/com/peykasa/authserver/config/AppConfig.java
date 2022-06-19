package com.peykasa.authserver.config;

import com.peykasa.authserver.editor.TimeConfig;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author Yaser(amin) Sadeghi
 */
@Configuration
public class AppConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);
    private String inactiveTime;
    private LoginPolicy loginPolicy;

    @Value("${app.password.encoder:plain}")
    private String encoder;

    @Getter
    @Value("${app.admin.view.enabled:true}")
    private boolean adminViewEnabled;

    @Value("${app.user.max.inactiveTime:-1}")
    public void setInactiveTime(String inactiveTime) {
        LOGGER.info("The setting inactive time string is '{}'", inactiveTime);
        this.inactiveTime = inactiveTime;
    }

    public Duration getInactiveTime() {
        LOGGER.info("The inactive time string is {}", inactiveTime);
        if ("-1".equals(inactiveTime))
            return null;
        return Duration.ofMillis(new TimeConfig(inactiveTime).getMillis());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        if ("hash".equalsIgnoreCase(encoder))
            return new OAuth2Config.HashEncoder();
        else if ("encrypt".equalsIgnoreCase(encoder))
            return new OAuth2Config.AESEncoder();
        else
            return NoOpPasswordEncoder.getInstance();

    }

    public LoginPolicy getLoginPolicy() {
        return loginPolicy;
    }

    @Autowired
    public void setLoginPolicy(LoginPolicy val) {
        this.loginPolicy = val;
    }

    @ConfigurationProperties("app.user.password")
    @Component
    public static class LoginPolicy {
        private Duration expireTime;
        private int invalidCount = -1;
        private Duration invalidCountIntervalTime;
        private Duration ignoreInvalidCountIntervalTime;


        public Duration getIgnoreInvalidCountIntervalTime() {
            return ignoreInvalidCountIntervalTime;
        }

        public void setIgnoreInvalidCountIntervalTime(Duration val) {
            this.ignoreInvalidCountIntervalTime = val;
        }

        public Duration getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(Duration val) {
            this.expireTime = val;
        }

        public int getInvalidCount() {
            return invalidCount;
        }

        public void setInvalidCount(int val) {
            this.invalidCount = val;
        }

        public Duration getInvalidCountIntervalTime() {
            return invalidCountIntervalTime;
        }

        public void setInvalidCountIntervalTime(Duration val) {
            this.invalidCountIntervalTime = val;
        }
    }
}
