package com.peykasa.authserver.exception;

import com.peykasa.authserver.model.SimpleRole;
import com.peykasa.authserver.model.cto.CreateUserCTO;
import com.peykasa.authserver.model.cto.RoleCTO;
import com.peykasa.authserver.model.dto.BaseDTO;
import com.peykasa.authserver.model.entity.Permission;
import com.peykasa.authserver.model.entity.Role;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

import javax.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Yaser(amin) Sadeghi
 */
public class ExceptionTranslator {
    private ExceptionTranslator() {
    }

    public static Exception translateDeleteRole(Exception e, String msg, Long id) {
        if (e instanceof EmptyResultDataAccessException && e.getMessage() != null && e.getMessage().contains("No class")) {
            return new ResourceNotFoundException(e, String.format(msg, id));
        }
        if (sqlException(e)) {
            if (e.getCause().getCause().getMessage() != null && e.getCause().getCause().getMessage().contains("tbl_user_role"))
                return new UnprocessableException(e, "Could not delete role ,role id " + id + " assigned to the some users");
        }
        return e;
    }

    public static Exception translateDeleteUser(Exception e, String msg, Long id) {
        if (e instanceof EmptyResultDataAccessException && e.getMessage() != null && e.getMessage().contains("No class")) {
            return new ResourceNotFoundException(e, String.format(msg, id));
        }
        return e;
    }

    public static Exception translateCreateUser(Exception e, CreateUserCTO cto) {
        if (sqlException(e)) {
            if (roleNotFound(e))
                return new ResourceNotFoundException(e, "Some roles in list" + cto.getRoles().stream().map(SimpleRole::getId).sorted().collect(Collectors.toList()) + " not found");
            if (dupplicate(e))
                return new DuplicateResourceException(e, "Duplicate user name " + cto.getUsername());
        }
        return e;
    }

    public static Exception translateCreateRole(Exception e, RoleCTO cto) {
        if (sqlException(e)) {
            if (permNotFound(e))
                return new ResourceNotFoundException(e, "Some permissions in list" + cto.getPermissions().stream().map(BaseDTO::getId).sorted().collect(Collectors.toList()) + " not found");
            if (dupplicate(e))
                return new DuplicateResourceException(e, "Duplicate role name " + cto.getName());
        }
        return e;
    }

    public static Exception translateUpdateUser(Exception e, Collection<Role> roles, String username) {
        if (e instanceof JpaObjectRetrievalFailureException && e.getCause() != null && e.getCause() instanceof EntityNotFoundException) {
            return new ResourceNotFoundException(e, "Some roles in list" + roles.stream().map(Role::getId).sorted().collect(Collectors.toList()) + " not found");
        }
        if (sqlException(e)) {
            if (dupplicate(e))
                return new DuplicateResourceException(e, "Duplicate user name " + username);
        }
        return e;
    }

    public static Exception translateUpdateROle(Exception e, Collection<Permission> permissions, String name) {
        if (e instanceof JpaObjectRetrievalFailureException && e.getCause() != null && e.getCause() instanceof EntityNotFoundException) {
            return new ResourceNotFoundException(e, "Some permissions in list" + permissions.stream().map(Permission::getId).sorted().collect(Collectors.toList()) + " not found");
        }
        if (sqlException(e)) {
            if (dupplicate(e))
                return new DuplicateResourceException(e, "Duplicate role name " + name);
        }
        return e;
    }

    private static boolean roleNotFound(Exception e) {
        return e.getCause().getCause().getMessage() != null && e.getCause().getCause().getMessage().contains("role_id");
    }

    private static boolean permNotFound(Exception e) {
        return e.getCause().getCause().getMessage() != null && e.getCause().getCause().getMessage().contains("permission_id");
    }

    private static boolean dupplicate(Exception e) {
        return e.getCause().getCause().getMessage() != null && e.getCause().getCause().getMessage().contains("Duplicate entry");
    }

    private static boolean sqlException(Exception e) {
        return e instanceof DataIntegrityViolationException &&
                e.getCause() != null &&
                e.getCause() instanceof ConstraintViolationException &&
                e.getCause().getCause() != null ;

    }
}
