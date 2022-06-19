package com.peykasa.authserver.controller;

import com.peykasa.authserver.exception.ResourceNotFoundException;
import com.peykasa.authserver.model.cto.BaseCTO;
import com.peykasa.authserver.model.dto.UserDTO;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.tools.SysLogger;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.Collection;

/**
 * @author Yaser(amin) Sadeghi
 */
@SuppressWarnings("unused")
public abstract class AuthenticatedUserController extends EntityController<UserDTO, BaseCTO, User, Long> {
    protected final static Logger LOGGER = LoggerFactory.getLogger(AuthenticatedUserController.class);

    private final ConsumerTokenServices consumerTokenServices;
    private final TokenStore tokenStore;
    public UserRepository userRepository;

    AuthenticatedUserController(ConsumerTokenServices consumerTokenServices, TokenStore tokenStore, UserRepository userRepository) {
        this.consumerTokenServices = consumerTokenServices;
        this.tokenStore = tokenStore;
        this.userRepository = userRepository;
    }

    User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User) {
            final String username = ((User) authentication.getPrincipal()).getUsername();
            var optional = userRepository.findByUsername(username);
            if (optional.isPresent())
                return optional.get();
        }
        return null;
    }

    void logout(User user) throws ResourceNotFoundException {
        if (user == null) {
            SysLogger.log("Logout failed ,user not login");
            throw new ResourceNotFoundException(null, "User not login");
        }
        Collection<OAuth2AccessToken> oAuth2AccessTokens = tokenStore.findTokensByClientIdAndUserName(user.getClient().getClientId(), user.getUsername());
        if (oAuth2AccessTokens == null) {
            SysLogger.log("Logout failed ,user not login");
            throw new ResourceNotFoundException(null, "User not login");
        }
        for (OAuth2AccessToken token : oAuth2AccessTokens) {
            consumerTokenServices.revokeToken(token.getValue());
        }
        SysLogger.log("User '" + user.getUsername()+"' logged out successfully");
    }

}
