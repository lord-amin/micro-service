package com.peykasa.authserver.audit.aspect.provider.User;

import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditUserDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.model.SimpleRole;
import com.peykasa.authserver.tools.SysLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class UserCreateProvider extends UserAuditProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserCreateProvider.class);

    public UserCreateProvider(Object returning, Object[] params) {
        super(returning, params);
    }

    @Override
    public Audit<?, ?> provide() {
        if (toUser == null) {
            LOGGER.error("The to provider of create user is null ");
            return null;
        }
        AuditUserDTO to = new AuditUserDTO();
        to.setId(toUser.getId());
        to.setUsername(toUser.getUsername());
        to.setFirstName(toUser.getFirstName());
        to.setLastName(toUser.getLastName());
        to.setEnabled(toUser.isEnabled());
        if (toUser.getRoles() != null)
            to.setRoles(toUser.getRoles().stream().map(SimpleRole::getName).sorted().collect(Collectors.toList()));
        SysLogger.log("User '" + toUser.getUsername() + "' created successfully by user '" + AuditUtil.getLoggedInUser()+"'");
        return new Audit<>(null, to);
    }
}
