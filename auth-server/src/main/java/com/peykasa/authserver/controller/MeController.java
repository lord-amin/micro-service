package com.peykasa.authserver.controller;

import com.peykasa.authserver.Constants;
import com.peykasa.authserver.audit.aspect.Auditable;
import com.peykasa.authserver.audit.aspect.provider.User.UserChangePassExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.User.UserChangePassProvider;
import com.peykasa.authserver.audit.aspect.provider.User.UserUpdateExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.User.UserUpdateProvider;
import com.peykasa.authserver.audit.aspect.provider.login.LogoutExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.login.LogoutProvider;
import com.peykasa.authserver.exception.ExceptionTranslator;
import com.peykasa.authserver.exception.PermissionDeniedException;
import com.peykasa.authserver.exception.ResourceNotFoundException;
import com.peykasa.authserver.exception.ValidationException;
import com.peykasa.authserver.model.MePassword;
import com.peykasa.authserver.model.SimpleRole;
import com.peykasa.authserver.model.cto.UserCTO;
import com.peykasa.authserver.model.dto.UserDTO;
import com.peykasa.authserver.model.entity.Role;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.BaseRepository;
import com.peykasa.authserver.repository.RoleRepository;
import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.service.UserService;
import com.peykasa.authserver.utility.ObjectMapper;
import com.peykasa.authserver.validation.ValidationContext;
import lombok.var;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.peykasa.authserver.Constants.FIRST_NAME_EMPTY;
import static com.peykasa.authserver.Constants.LAST_NAME_EMPTY;
import static com.peykasa.authserver.utility.ObjectMapper.to;

//import io.swagger.annotations.Api;

/**
 * @author Taher Khorshidi, Yaser(amin) Sadeghi
 */
//@Api(tags = "User Information of Token", produces = "application/json")
@RestController
@RequestMapping("/api/me")
public class MeController extends AuthenticatedUserController {
    protected final static Logger LOGGER = LoggerFactory.getLogger(MeController.class);
    private final UserRepository userRepository;
    private final ValidationContext<UserCTO> updateUserValidator;
    private final UserService userService;
    private final RoleRepository roleRepository;

    @Autowired
    public MeController(UserRepository userRepository, UserService userService, ConsumerTokenServices consumerTokenServices, TokenStore tokenStore, RoleRepository roleRepository) {
        super(consumerTokenServices, tokenStore, userRepository);
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.updateUserValidator = new ValidationContext<>();
        this.userRepository = userRepository;
        this.updateUserValidator.addValidation(userCTO -> "".equals(StringUtils.trim(userCTO.getFirstName())) ? Collections.singletonList(FIRST_NAME_EMPTY) : null);
        this.updateUserValidator.addValidation(userCTO -> "".equals(StringUtils.trim(userCTO.getLastName())) ? Collections.singletonList(LAST_NAME_EMPTY) : null);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public User me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User) {
            final String username = ((User) authentication.getPrincipal()).getUsername();
            var optional = userRepository.findByUsername(username);
            if (!optional.isPresent())
                return null;
            User byUsername = optional.get();
            byUsername.setPassword("****");
            return byUsername;
        }
        return null;
    }

    @Auditable(provider = UserChangePassProvider.class, exProvider = UserChangePassExceptionProvider.class, context = Constants.USER, event = Constants.CHANGE_PASSWORD)
    @RequestMapping(path = Constants.USER_CHANGE_PASSWORD_PATH, method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody MePassword data) throws ValidationException, ResourceNotFoundException {
        var authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null)
            throw new ResourceNotFoundException(null, "User not found");
        userService.changePassword(authenticatedUser, data);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Auditable(provider = UserUpdateProvider.class, exProvider = UserUpdateExceptionProvider.class, context = Constants.USER, event = Constants.UPDATE)
    @RequestMapping(method = RequestMethod.PATCH)
    @ResponseBody
    public UserDTO updateMe(@RequestBody UserCTO cto) throws Exception {
        User found = getAuthenticatedUser();
        if (found == null)
            throw new ResourceNotFoundException(null, String.format(Constants.USER_NOT_FOUND, "Current user not found"));
        updateUserValidator.validate(cto);
        User preDataBaseState = found.copy();
        UserCTO preUserCTO = cto.copy();
        if (cto.getFirstName() != null)
            found.setFirstName(cto.getFirstName());
        if (cto.getLastName() != null)
            found.setLastName(cto.getLastName());
        if (cto.getRoles() != null) {
            if (found.getSuperAdmin())
                throw new PermissionDeniedException("you cannot change your role. you are super admin");
            Set<Role> newRole = new HashSet<>();
            for (SimpleRole role : cto.getRoles()) {
                if (found.getRoles().stream().anyMatch(m -> m.getName().equals(role.getName()))) {
                    final Optional<Role> optional = roleRepository.findByName(role.getName());
                    optional.ifPresent(newRole::add);
                }
            }

            found.setRoles(newRole);
        }
        found.setModifiedDate(new Date());
        // hold pre provider for auditing
        if (cto.getFirstName() != null)
            cto.setFirstName(preDataBaseState.getFirstName());
        if (cto.getLastName() != null)
            cto.setLastName(preDataBaseState.getLastName());
        if (cto.getRoles() != null)
            cto.setRoles(preDataBaseState.getRoles().stream().map(role -> new SimpleRole(role.getId(), role.getName())).collect(Collectors.toList()));
        User updated;
        try {
            updated = userRepository.saveAndFlush(found);
        } catch (Exception e) {
            ObjectMapper.copy(preUserCTO, cto);
            throw ExceptionTranslator.translateUpdateUser(e, found.getRoles(), null);
        }
        return to(updated, UserDTO.class);
    }

    @Auditable(provider = LogoutProvider.class, exProvider = LogoutExceptionProvider.class, context = Constants.AUTH, event = Constants.LOGOUT)
    @RequestMapping(method = RequestMethod.GET, path = Constants.USER_LOGOUT_PATH)
    public ResponseEntity<?> logout() throws ResourceNotFoundException {
        User authenticatedUser = getAuthenticatedUser();
        logout(authenticatedUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public BaseRepository<User, Long> getRepository() {
        return userRepository;
    }

    @Override
    protected Class<UserDTO> support() {
        return UserDTO.class;
    }

    @Override
    public String notFoundMessage() {
        return Constants.USER_NOT_FOUND;
    }
}
