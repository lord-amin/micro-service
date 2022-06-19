package com.peykasa.authserver.controller;

import com.peykasa.authserver.Constants;
import com.peykasa.authserver.config.APIConfig;
import com.peykasa.authserver.config.ResourceServerConfig;
import com.peykasa.authserver.exception.PermissionDeniedException;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.UserRepository;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthorizationController {
    protected final static Logger LOGGER = LoggerFactory.getLogger(AuthorizationController.class);
    private final ResourceServerConfig resourceServerConfig;
    private final UserRepository userRepository;

    @Autowired
    public AuthorizationController(ResourceServerConfig resourceServerConfig, UserRepository userRepository) {
        this.resourceServerConfig = resourceServerConfig;
        this.userRepository = userRepository;
    }


    @PostMapping(path = Constants.AUTHORIZES_URL)
    public List<ResourceServerConfig.Triple> authorize(@RequestBody List<APIConfig> urls, Principal principal) {
        var optional = userRepository.findByUsername(principal.getName());
        User user = null;
        if (optional.isPresent())
            user = optional.get();
        return resourceServerConfig.check(user, urls);
    }

    @PostMapping(path = Constants.AUTHORIZE_URL)
    public User authorize(@RequestBody APIConfig url, Principal principal) throws PermissionDeniedException {
        LOGGER.warn(">> ur {}", userRepository);
        LOGGER.warn(">> p {}", principal);
        var optional = userRepository.findByUsername(principal.getName());
        User user = null;
        if (optional.isPresent())
            user = optional.get();
        List<ResourceServerConfig.Triple> check = resourceServerConfig.check(user, Collections.singletonList(url));
        if (check.get(0).isAccess()) {
            user.setPassword("*****");
            return user;
        }

        throw new PermissionDeniedException("permission denied");
    }
}
