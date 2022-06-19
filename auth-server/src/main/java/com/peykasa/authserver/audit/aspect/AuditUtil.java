package com.peykasa.authserver.audit.aspect;

import com.peykasa.authserver.Constants;
import com.peykasa.authserver.model.MePassword;
import com.peykasa.authserver.model.UserPassword;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.UserRepository;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * @author Yaser(amin) Sadeghi
 */

public abstract class AuditUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditUtil.class);

    public static String getLoggedInUser() {
        LOGGER.info("Sending in thread {} ", Thread.currentThread().getName());
        var user = "anonymous";
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return user;
        var principal = authentication.getPrincipal();
        if (principal instanceof User) {
            user = ((User) principal).getUsername();
        }
        return user;
    }

    public static boolean isSuperAdmin() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            var user = ((User) principal);
            return user.getSuperAdmin() == null ? false : user.getSuperAdmin();
        }
        return false;
    }

    public static String getUser(UserRepository userRepository, Object pass) {
        if (pass == null) {
            return "--";
        }
         LOGGER.warn(">>>>>> {}", pass.getClass());
        if (pass.getClass().equals(MePassword.class)) {
            return AuditUtil.getLoggedInUser();
        } else if (pass.getClass().equals(UserPassword.class)) {
            UserPassword userPass = (UserPassword) pass;
            if (userPass.getId() == null || userPass.getId() < 1)
                return "---";
            return getUser(userRepository, userPass.getId());
        } else {
            return "-";
        }
    }

    public static String getUser(UserRepository userRepository, Long id) {
        if (id == null)
            return "-----";
        String byId = userRepository.getUserById(id);
        if (byId==null)
            return "" + id;
        if(byId.contains(Constants._DELETED_AT)){
            int i = byId.indexOf(Constants._DELETED_AT);
            byId=byId.substring(0,i);
        }
        return byId;
    }
}