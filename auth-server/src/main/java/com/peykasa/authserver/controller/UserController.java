package com.peykasa.authserver.controller;

import com.peykasa.authserver.Constants;
import com.peykasa.authserver.audit.aspect.Auditable;
import com.peykasa.authserver.audit.aspect.provider.GetExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.SearchProvider;
import com.peykasa.authserver.audit.aspect.provider.User.*;
import com.peykasa.authserver.config.AppConfig;
import com.peykasa.authserver.exception.ExceptionTranslator;
import com.peykasa.authserver.exception.PermissionDeniedException;
import com.peykasa.authserver.exception.ResourceNotFoundException;
import com.peykasa.authserver.exception.ValidationException;
import com.peykasa.authserver.model.SimpleRole;
import com.peykasa.authserver.model.UserPassword;
import com.peykasa.authserver.model.cto.CreateUserCTO;
import com.peykasa.authserver.model.cto.UserCTO;
import com.peykasa.authserver.model.dto.UserDTO;
import com.peykasa.authserver.model.entity.Client;
import com.peykasa.authserver.model.entity.DeletedUser;
import com.peykasa.authserver.model.entity.Role;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.BaseRepository;
import com.peykasa.authserver.repository.DeletedUserRepository;
import com.peykasa.authserver.repository.RoleRepository;
import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.service.UserService;
import com.peykasa.authserver.validation.PasswordRules;
import com.peykasa.authserver.validation.ValidationConfiguration;
import com.peykasa.authserver.validation.ValidationContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static com.peykasa.authserver.Constants.*;
import static com.peykasa.authserver.utility.ObjectMapper.to;
import static org.springframework.http.ResponseEntity.ok;

//import io.swagger.annotations.Api;

/**
 * @author Yaser(amin) Sadeghi
 */
//@Api(tags = "User controller", description = "User controller", produces = "application/json")
@RequestMapping(value = Constants.USER_CONTEXT_PATH)
@RepositoryRestController
public class UserController extends AuthenticatedUserController {
    protected final static Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppConfig appConfig;
    private final RoleRepository roleRepository;
    private ValidationContext<CreateUserCTO> createUserValidator;
    private ValidationContext<CreateUserCTO> updateUserValidator;
    protected final DeletedUserRepository repository;
    private final UserService userService;
    @Autowired
    HttpServletRequest httpServletRequest;
    @PersistenceContext
    private EntityManager em;
    private static final String ERROR_UNHANDLED = "unhandled_error";

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, AppConfig appConfig,
                          DeletedUserRepository repository, ValidationConfiguration validationConfiguration, ConsumerTokenServices consumerTokenServices,
                          UserService userService, TokenStore tokenStore, RoleRepository roleRepository) {
        super(consumerTokenServices, tokenStore, userRepository);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appConfig = appConfig;
        this.repository = repository;
        this.userService = userService;
        this.roleRepository = roleRepository;
        createCtx(validationConfiguration);
        updateCtx(validationConfiguration);

    }

    private void createCtx(ValidationConfiguration validationConfiguration) {
        this.createUserValidator = new ValidationContext<>();
        this.createUserValidator.addValidation(userCTO -> StringUtils.isEmpty(StringUtils.trim(userCTO.getFirstName())) ? Collections.singletonList(FIRST_NAME_NULL) : null);
        this.createUserValidator.addValidation(userCTO -> StringUtils.isEmpty(StringUtils.trim(userCTO.getLastName())) ? Collections.singletonList(LAST_NAME_NULL) : null);
        this.createUserValidator.addValidation(userCTO -> StringUtils.isEmpty(StringUtils.trim(userCTO.getUsername())) ? Collections.singletonList(USER_NAME_NULL) : null);
        this.createUserValidator.addValidation(userCTO -> (userCTO.getUsername()).contains(" ") ? Collections.singletonList(SPACE_NOT) : null);
        this.createUserValidator.addValidation(userCTO -> StringUtils.isEmpty(userCTO.getPassword()) ? Collections.singletonList(PASSWORD_NULL) : null);
        this.createUserValidator.addValidation(userCTO -> new PasswordRules().validate(userCTO.getPassword(), validationConfiguration.getValidation()));
        this.createUserValidator.addValidation(createUserCTO -> {
            if (!StringUtils.isAsciiPrintable(createUserCTO.getUsername()))
                return Collections.singletonList(MessageFormat.format(VALIDATION_NON_PRINTABLE, createUserCTO.getUsername()));
            return null;
        });
    }

    private void updateCtx(ValidationConfiguration validationConfiguration) {
        updateUserValidator = new ValidationContext<>();
        this.updateUserValidator.addValidation(userCTO -> StringUtils.isBlank(StringUtils.trim(userCTO.getFirstName())) ? Collections.singletonList(FIRST_NAME_NULL) : null);
        this.updateUserValidator.addValidation(userCTO -> StringUtils.isBlank(StringUtils.trim(userCTO.getLastName())) ? Collections.singletonList(LAST_NAME_NULL) : null);
        this.updateUserValidator.addValidation(createUserCTO -> {
            if (!StringUtils.isAsciiPrintable(createUserCTO.getUsername()))
                return Collections.singletonList(MessageFormat.format(VALIDATION_NON_PRINTABLE, createUserCTO.getUsername()));
            return null;
        });
        updateUserValidator.addValidation(m -> m.getUsername() != null && m.getUsername().contains(" ") ? Collections.singletonList(SPACE_NOT) : null);
        updateUserValidator.addValidation(m -> m.getPassword() != null && "".equals(m.getPassword()) ? Collections.singletonList(PASSWORD_NULL) : null);
        updateUserValidator.addValidation(m -> {
            if (m.getPassword() == null)
                return null;
            return new PasswordRules().validate(m.getPassword(), validationConfiguration.getValidation());
        });
    }

    @Auditable(provider = SearchProvider.class, context = Constants.USER, event = Constants.SEARCH)
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public PagedResources<Resource<UserDTO>> findAll(String page, String size, String sort) {
        return super.findAll(page, size, sort);
    }

    @Auditable(provider = UserGetProviderUser.class, exProvider = GetExceptionProvider.class, context = Constants.USER, event = Constants.SEARCH)
    @RequestMapping(path = Constants.USER_GET, method = RequestMethod.GET)
    @ResponseBody
    public UserDTO findOne(@PathVariable("id") Long id) throws ResourceNotFoundException {
        return super.findOne(id);
    }

    @Override
    public String notFoundMessage() {
        return Constants.USER_NOT_FOUND;
    }

    @RequestMapping(path = Constants.USER_ROLES_PATH, method = RequestMethod.GET)
    @ResponseBody
    public Resources<SimpleRole> userRoles(@PathVariable("id") Long id) throws ResourceNotFoundException {
        User one = userRepository.findOne(id);
        if (one == null)
            throw new ResourceNotFoundException(null, String.format(notFoundMessage(), id));
        return new Resources<>(to(one.getRoles(), SimpleRole.class));
    }

    @Auditable(provider = UserCreateProvider.class, exProvider = UserCreateExceptionProvider.class, context = Constants.USER, event = Constants.CREATE)
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public UserDTO createUser(@RequestBody CreateUserCTO userCTO) throws Exception {
        createUserValidator.validate(userCTO);
        userCTO.setPassword(passwordEncoder.encode(userCTO.getPassword()));
        User user = to(userCTO, User.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new ResourceNotFoundException(null, "Client not found ");
        final Client client = ((User) authentication.getPrincipal()).getClient();
        user.setClient(client);
        Date now = new Date();
        user.setCreationDate(now);
        user.setModifiedDate(now);
        userCTO.setFirstName(userCTO.getFirstName() != null ? userCTO.getFirstName().trim() : userCTO.getFirstName());
        userCTO.setLastName(userCTO.getLastName() != null ? userCTO.getLastName().trim() : userCTO.getLastName());
//         set password expiry
        Duration expireTime = appConfig.getLoginPolicy().getExpireTime();
        if (expireTime != null && expireTime.toMillis() > 0) {
            user.setPasswordExpiry(now, expireTime.toMillis());
        }
        User saved;
        try {
            saved = userRepository.saveAndFlush(user);
            em.detach(saved);
        } catch (Exception e) {
            throw ExceptionTranslator.translateCreateUser(e, userCTO);
        }
        Long id = saved.getId();
        saved = userRepository.findOne(id);
        if (saved == null) {
            throw new ResourceNotFoundException(null, String.format(notFoundMessage(), id));
        }
        return to(saved, UserDTO.class);
    }


    @Auditable(provider = UserUpdateProvider.class, exProvider = UserUpdateExceptionProvider.class, context = Constants.USER, event = UPDATE)
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public UserDTO updateUser(@RequestBody CreateUserCTO req) throws Exception {
        updateUserValidator.validate(req);
        final User loginUser = getLoginUser();
        final User foundInDb = fetchUser(req.getId());
        httpServletRequest.setAttribute("before", to(foundInDb, UserCTO.class));
        if (foundInDb.getSuperAdmin())
            throw new PermissionDeniedException("You cannot edit super admin user");
        if (isTheSamePerson(loginUser, foundInDb)) {
            throw new PermissionDeniedException("Use another api to change your profile");
        }
        return to(editUser(foundInDb, to(req, User.class)), UserDTO.class);
    }


    private boolean isTheSamePerson(User loginUser, User foundInDb) {
        return loginUser.getId().equals(foundInDb.getId());
    }

    private User getLoginUser() throws ResourceNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new ResourceNotFoundException(null, "Client not found ");

        return (User) authentication.getPrincipal();
    }

    private User editUser(User foundInDb, User req) {
        Date now = new Date();
        foundInDb.setModifiedDate(now);
        if (req.getFirstName() != null)
            foundInDb.setFirstName(req.getFirstName());
        if (req.getLastName() != null)
            foundInDb.setLastName(req.getLastName());
//        if (req.getClient() != null)
//            foundInDb.setClient(req.getClient());
//        if (req.isEnabled())
        foundInDb.setEnabled(req.isEnabled());
        if (req.getRoles() != null) {
            foundInDb.setRoles(new HashSet<>());
            for (Role role : req.getRoles()) {
                final Optional<Role> optional = roleRepository.findByName(role.getName());
                optional.ifPresent(value -> foundInDb.getRoles().add(value));

            }
        }
        Duration expireTime = appConfig.getLoginPolicy().getExpireTime();
        if (expireTime != null && expireTime.toMillis() > 0) {
            foundInDb.setPasswordExpiry(now, expireTime.toMillis());
        }

        return userRepository.saveAndFlush(foundInDb);
    }

    private User fetchUser(Long id) throws ResourceNotFoundException {
        final Optional<User> foundInDb = userRepository.findById(id);
        if (!foundInDb.isPresent())
            throw new ResourceNotFoundException(null, "User not found");
        return foundInDb.get();
    }


    @Auditable(provider = UserDeleteProvider.class, exProvider = UserDeleteExceptionProvider.class, context = Constants.USER, event = Constants.DELETE)
    @RequestMapping(path = Constants.USER_GET, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) throws Exception {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null)
            throw new ResourceNotFoundException(null, "User not found");
        if (authenticatedUser.getId().equals(id)) {
            throw new PermissionDeniedException(CAN_NOT_DELETE_YOURSELF);
        }
        if (id == null || id < 1)
            throw new ResourceNotFoundException(null, "Empty id " + id);
        try {
            User found = userRepository.findOne(id);
            if (found == null)
                throw new ResourceNotFoundException(null, String.format(notFoundMessage(), id));
            if (found.getSuperAdmin() != null && found.getSuperAdmin()) {
                if (authenticatedUser.getSuperAdmin() != null && !authenticatedUser.getSuperAdmin()) {
                    throw new PermissionDeniedException(ACCESS_DENIED_ADMIN);
                }
            }
            userRepository.delete(found);
            logout(found);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            throw ExceptionTranslator.translateDeleteUser(e, notFoundMessage(), id);
        }
    }


    @RequestMapping(method = RequestMethod.GET, path = Constants.USER_DELETED_PATH)
    @ResponseBody
    public ResponseEntity<?> get(Integer size, Integer page) {
        final Page<DeletedUser> paged;
        if (size != null && page != null)
            paged = repository.findAll(new PageRequest(page, size));
        else
            paged = repository.findAll((Pageable) null);
        PagedResources.PageMetadata meta = new PagedResources.PageMetadata(paged.getSize() == 0 ? paged.getTotalElements() : paged.getSize(), paged.getNumber(),
                paged.getTotalElements(), paged.getTotalPages());
        PagedResources<DeletedUser> pagedResources = new PagedResources<>(paged.getContent(), meta);
        return ok(pagedResources);
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
    protected Page<User> findAll(Pageable pageable) {
        if (appConfig.isAdminViewEnabled())
            return userRepository.findAll(pageable);
        else
            return userRepository.findBySuperAdmin(false, pageable);
    }

    @Auditable(provider = UserChangePassProvider.class, exProvider = UserChangePassExceptionProvider.class, context = Constants.USER, event = Constants.CHANGE_PASSWORD)
    @RequestMapping(path = Constants.USER_CHANGE_PASSWORD_PATH, method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody UserPassword data) throws ValidationException {
        if (data.getId() == null || data.getId() < 1)
            throw new ValidationException(String.format(USER_NOT_FOUND, data.getId()));
        User user = userRepository.findOne(data.getId());
        userService.changePassword(user, data);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Transactional
    @ResponseBody
    @PostMapping(value = "/admin/reset")
    public String restAdmin() {
        Optional<User> userOpt = userRepository.findByUsername("admin");
        if (userOpt.isPresent()) {
            User adminUser = userOpt.get();
            adminUser.setPassword(passwordEncoder.encode("1"));
            getRepository().save(adminUser);
            return "reset admin password is done.";
        }
        return "something goes wrong on the server";
    }
}