package com.peykasa.authserver.audit.aspect.provider.User;

import com.peykasa.authserver.audit.aspect.provider.dto.AuditUserDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.model.SimpleRole;
import com.peykasa.authserver.model.cto.UserCTO;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class UserUpdateProvider extends UserAuditProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserUpdateProvider.class);
    private UserCTO fromUser;

    public UserUpdateProvider(Object returning, Object[] params) {
        super(returning, params);
        fromUser = (UserCTO) params[0];
    }

    @Override
    public Audit<AuditUserDTO, AuditUserDTO> provide() {
        if (toUser == null) {
            LOGGER.error("The to provider of create user is null ");
            return null;
        }
        var from = new AuditUserDTO();
        var to = new AuditUserDTO();
        // audit for update user
        if (fromUser.getFirstName() != null) {
            if (!fromUser.getFirstName().equals(toUser.getFirstName())) {
                to.setFirstName(toUser.getFirstName());
                from.setFirstName(fromUser.getFirstName());
            }
        }
        if (fromUser.getLastName() != null) {
            if (!fromUser.getLastName().equals(toUser.getLastName())) {
                to.setLastName(toUser.getLastName());
                from.setLastName(fromUser.getLastName());
            }
        }
        if (fromUser.getRoles() != null) {
            fromUser.getRoles().sort(Comparator.comparing(SimpleRole::getName));
            List<String> rolesTo = toUser.getRoles().stream().map(SimpleRole::getName).sorted().collect(Collectors.toList());
            List<String> rolesFrom = fromUser.getRoles().stream().map(SimpleRole::getName).sorted().collect(Collectors.toList());
            if (!rolesFrom.toString().equals(rolesTo.toString())) {
                to.setRoles(new ArrayList<>());
                from.setRoles(new ArrayList<>());
                to.getRoles().addAll(rolesTo);
                from.getRoles().addAll(rolesFrom);
            }
        }
        return new Audit<>(from, to);
    }
}
