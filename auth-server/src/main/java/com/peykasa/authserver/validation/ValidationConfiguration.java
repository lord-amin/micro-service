package com.peykasa.authserver.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author Yaser(amin) Sadeghi
 */
@Configuration
public class ValidationConfiguration {

//    @Bean
//    public UserValidator beforeCreateUserValidator() {
//        return new UserValidator(oAuth2Config.passwordEncoder());
//    }


    private Validation validation;

    public Validation getValidation() {
        return validation;
    }

    @Autowired
    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    @ConfigurationProperties("app.validation.password")
    @Component
    public static class Validation {
        private int maxLength = -1;
        private int minLength = -1;
        private boolean hasUpper = false;
        private boolean hasLower = false;
        private boolean hasSpecial = false;
        private boolean hasDigit = false;

        public int getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }

        public int getMinLength() {
            return minLength;
        }

        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }

        public boolean isHasUpper() {
            return hasUpper;
        }

        public void setHasUpper(boolean hasUpper) {
            this.hasUpper = hasUpper;
        }

        public boolean isHasLower() {
            return hasLower;
        }

        public void setHasLower(boolean hasLower) {
            this.hasLower = hasLower;
        }

        public boolean isHasSpecial() {
            return hasSpecial;
        }

        public void setHasSpecial(boolean hasSpecial) {
            this.hasSpecial = hasSpecial;
        }

        public boolean isHasDigit() {
            return hasDigit;
        }

        public void setHasDigit(boolean hasDigit) {
            this.hasDigit = hasDigit;
        }
    }
}
