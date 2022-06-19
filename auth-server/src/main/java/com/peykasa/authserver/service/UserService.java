package com.peykasa.authserver.service;


import com.peykasa.authserver.config.AppConfig;
import com.peykasa.authserver.exception.ValidationException;
import com.peykasa.authserver.model.MePassword;
import com.peykasa.authserver.model.UserPassword;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.validation.PasswordRules;
import com.peykasa.authserver.validation.ValidationConfiguration;
import com.peykasa.authserver.validation.ValidationContext;
import lombok.var;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static com.peykasa.authserver.Constants.*;

/**
 * @author Yaser(amin) Sadeghi
 */
@Service
public class UserService
        implements UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final PasswordEncoder passwordEncoder;
    private final AppConfig appConfig;
    private final ValidationContext<MePassword> mePasswordValidator;
    private final ValidationContext<UserPassword> userPasswordValidator;
    private final ValidationConfiguration validationConfiguration;

    private final UserRepository userRepository;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, AppConfig appConfig, ValidationConfiguration validationConfiguration, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.appConfig = appConfig;
        this.validationConfiguration = validationConfiguration;
        this.userRepository = userRepository;
        this.mePasswordValidator = new ValidationContext<>();
        this.userPasswordValidator = new ValidationContext<>();
        this.mePasswordValidator.addValidation(p -> StringUtils.isEmpty(p.getOldPassword()) ? Collections.singletonList(OLD_PASSWORD_NULL) : null);
        this.mePasswordValidator.addValidation(p -> StringUtils.isEmpty(p.getNewPassword()) ? Collections.singletonList(NEW_PASSWORD_NULL) : null);
        this.userPasswordValidator.addValidation(p -> StringUtils.isEmpty(p.getNewPassword()) ? Collections.singletonList(NEW_PASSWORD_NULL) : null);
        this.mePasswordValidator.addValidation(p -> StringUtils.isEmpty(p.getConfirmPassword()) ? Collections.singletonList(CONFIRM_PASSWORD_NULL) : null);
        this.userPasswordValidator.addValidation(p -> StringUtils.isEmpty(p.getConfirmPassword()) ? Collections.singletonList(CONFIRM_PASSWORD_NULL) : null);
        this.mePasswordValidator.addValidation(userCTO -> new PasswordRules().validate(userCTO.getNewPassword(), this.validationConfiguration.getValidation()));
        this.userPasswordValidator.addValidation(userCTO -> new PasswordRules().validate(userCTO.getNewPassword(), this.validationConfiguration.getValidation()));
        this.mePasswordValidator.addValidation(u -> !u.getNewPassword().equals(u.getConfirmPassword()) ? Collections.singletonList(CONFIRM_PASSWORD_FAIL) : null);
        this.userPasswordValidator.addValidation(u -> !u.getNewPassword().equals(u.getConfirmPassword()) ? Collections.singletonList(CONFIRM_PASSWORD_FAIL) : null);
        this.mePasswordValidator.addValidation(userPassword -> userPassword.getNewPassword().equals(userPassword.getOldPassword()) ? Collections.singletonList(OLD_AND_NEW_PASS_FAIL) : null);

    }

    public Optional<User> findByUsername(String username) {
        if (StringUtils.isEmpty(username))
            return Optional.empty();
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String clientId = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
        LOGGER.info("Finding user {} for login...", username);
        try {
            User byUsername = userRepository.findByUsernameAndClient_clientId(username, clientId);
            if (byUsername == null) {
                LOGGER.error("The username {} not found", username);
                throw new IllegalStateException("The username " + username + " not found");
            }
            return byUsername;
        } catch (Exception e) {
            LOGGER.error("", e);
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }


    @Transactional
    public void changePassword(User user, MePassword data) throws ValidationException {
        mePasswordValidator.validate(data);
        if (!passwordEncoder.matches(data.getOldPassword(), user.getPassword())) {
            throw new ValidationException(OLD_PASS_FAIL);
        }
        passChange(user, data.getNewPassword());
    }

    @Transactional
    public void changePassword(User user, UserPassword data) throws ValidationException {
        userPasswordValidator.validate(data);
        passChange(user, data.getNewPassword());
    }

    private void passChange(User user, String newPassword) {
        var now = new Date();
        user.setModifiedDate(now);
        Duration expireTime = appConfig.getLoginPolicy().getExpireTime();
        if (expireTime != null && expireTime.toMillis() > 0) {
            user.setPasswordExpiry(now, expireTime.toMillis());
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(user);
    }
}
