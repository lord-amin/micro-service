package com.peykasa.authserver.service;

import com.peykasa.authserver.Constants;
import com.peykasa.authserver.audit.aspect.Auditable;
import com.peykasa.authserver.audit.aspect.provider.login.LoginExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.login.LoginProvider;
import com.peykasa.authserver.config.AppConfig;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.UserRepository;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Calendar;
import java.util.Date;

import static com.peykasa.authserver.utility.DurationUtil.addDurationToCalendar;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
public class CustomAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationProvider.class);
    private final UserDetailsService userService;
    private final PasswordEncoder passwordEncoder;
    private final AppConfig appConfig;
    private final UserRepository userRepository;

    @Autowired
    public CustomAuthenticationProvider(UserDetailsService userSer, PasswordEncoder passEnc, AppConfig appConfig, UserRepository userRep) {
        LOGGER.info("Config custom auth provider");
        this.hideUserNotFoundExceptions = false;
        setPreAuthenticationChecks(user -> {
            if (!user.isAccountNonLocked()) {
                if (user instanceof User) {
                    var untilDate = ((User) user).getBlockDate().toString();
                    LOGGER.info("User account is locked until {} ", untilDate);
                    throw new LockedException("User account is locked until " + untilDate);
                } else {
                    LOGGER.debug("Account is locked ");
                    throw new LockedException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked", "account is locked"));
                }

            }
            if (!user.isEnabled()) {
                logger.debug("User account is disabled");
                throw new DisabledException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "User is disabled"));
            }
        });
        this.userService = userSer;
        this.passwordEncoder = passEnc;
        this.appConfig = appConfig;
        this.userRepository = userRep;
    }

    @Override
    protected void additionalAuthenticationChecks(final UserDetails userDetails, final UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        LOGGER.info("additional custom auth provider");
        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        var presentedPassword = authentication.getCredentials().toString();
        if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            logger.debug("Authentication failed: password does not match stored value");
            var blockDuration = appConfig.getLoginPolicy().getInvalidCountIntervalTime();
            int invalidCount = appConfig.getLoginPolicy().getInvalidCount();
            if (blockDuration != null && invalidCount > 0) {
                LOGGER.info("The block user is active");
                if (userDetails instanceof User) {
                    var one = userRepository.findOne(((User) userDetails).getId());
                    one.setBlockCount((one.getBlockCount() == null ? 0 : one.getBlockCount()) + 1);
                    if (one.getBlockCount() >= invalidCount) {
                        LOGGER.warn("The user {} is reached block count ", userDetails.getUsername());
                        one.setBlockCount(0);
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.MILLISECOND, (int) blockDuration.toMillis());
                        one.setBlockDate(calendar.getTime());
                    }
                    userRepository.saveAndFlush(one);
                }
            }
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }
    }

    @Override
    protected UserDetails retrieveUser(final String username, final UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        UserDetails loadedUser;
        LOGGER.info("retrieve custom auth provider");
        try {
            loadedUser = userService.loadUserByUsername(username);
            if ((loadedUser instanceof User)) {
                User found = (User) loadedUser;
                var lastLoginAttemptTime = found.getLastLoginAttemptTime();
                var now = new Date();
                var failedAttemptPeriod = appConfig.getLoginPolicy().getIgnoreInvalidCountIntervalTime();
                if (failedAttemptPeriod != null && lastLoginAttemptTime != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(lastLoginAttemptTime);
                    calendar.add(Calendar.MILLISECOND, (int) failedAttemptPeriod.toMillis());
                    if (now.after(calendar.getTime())) {
                        found.setBlockCount(null);
                        found.setBlockDate(null);
                    }
                }
                LOGGER.info("The inactive time is {}", appConfig.getInactiveTime());
                if (found.getSuperAdmin() != null && !found.getSuperAdmin())
                    if (appConfig.getInactiveTime() != null && lastLoginAttemptTime != null) {
                        LOGGER.info("Checking inactive time");
                        var calendar = addDurationToCalendar(lastLoginAttemptTime, appConfig.getInactiveTime().toMillis());
                        if (new Date().after(calendar.getTime())) {
                            found.setEnabled(false);
                            LOGGER.info("Disabling user ");
                        }
                    }
                found.setLastLoginAttemptTime(now);
                userRepository.saveAndFlush(found);
            }
        } catch (UsernameNotFoundException notFound) {
            throw notFound;
        } catch (Exception repositoryProblem) {
            throw new InternalAuthenticationServiceException(
                    repositoryProblem.getMessage(), repositoryProblem);
        }

        if (loadedUser == null) {
            throw new InternalAuthenticationServiceException(
                    "UserDetailsService returned null, which is an interface contract violation");
        }
        return loadedUser;
    }

    @Auditable(provider = LoginProvider.class, exProvider = LoginExceptionProvider.class, context = Constants.AUTH, event = Constants.LOGIN)
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.onlySupports",
                        "Only UsernamePasswordAuthenticationToken is supported"));

        // Determine username
        String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED"
                : authentication.getName();

        boolean cacheWasUsed = true;
        UserDetails user = getUserCache().getUserFromCache(username);

        if (user == null) {
            cacheWasUsed = false;
            try {
                user = retrieveUser(username,
                        (UsernamePasswordAuthenticationToken) authentication);
            } catch (UsernameNotFoundException notFound) {
                logger.debug("User '" + username + "' not found");
                if (hideUserNotFoundExceptions) {
                    throw new BadCredentialsException(notFound.getMessage());
                } else {
                    throw notFound;
                }
            }

            Assert.notNull(user,
                    "retrieveUser returned null - a violation of the interface contract");
        }

        try {
            getPreAuthenticationChecks().check(user);
            additionalAuthenticationChecks(user,
                    (UsernamePasswordAuthenticationToken) authentication);
        } catch (AuthenticationException exception) {
            if (cacheWasUsed) {
                // There was a problem, so try again after checking
                // we're using latest data (i.e. not from the cache)
                cacheWasUsed = false;
                user = retrieveUser(username,
                        (UsernamePasswordAuthenticationToken) authentication);
                getPreAuthenticationChecks().check(user);
                additionalAuthenticationChecks(user,
                        (UsernamePasswordAuthenticationToken) authentication);
            } else {
                throw exception;
            }
        }

        getPostAuthenticationChecks().check(user);

        if (!cacheWasUsed) {
            getUserCache().putUserInCache(user);
        }

        Object principalToReturn = user;

        if (isForcePrincipalAsString()) {
            principalToReturn = user.getUsername();
        }
        return createSuccessAuthentication(principalToReturn, authentication, user);
    }

    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails userDetails) {
        Authentication successAuthentication = super.createSuccessAuthentication(principal, authentication, userDetails);
        if (userDetails instanceof User) {
            User one = userRepository.findOne(((User) userDetails).getId());
            if (one == null) {
                throw new BadCredentialsException("Bad credentials in create");
            }
            one.setBlockCount(null);
            one.setBlockDate(null);
            userRepository.saveAndFlush(one);
        }
        return successAuthentication;
    }
}