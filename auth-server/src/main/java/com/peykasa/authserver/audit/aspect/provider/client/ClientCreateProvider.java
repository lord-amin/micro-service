package com.peykasa.authserver.audit.aspect.provider.client;

import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditClientDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.model.dto.UserClientDTO;
import com.peykasa.authserver.tools.SysLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class ClientCreateProvider extends AuditProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCreateProvider.class);
    private UserClientDTO toUser;

    public ClientCreateProvider(Object returning, Object[] params) {
        super(returning, params);
        try {
            toUser = (UserClientDTO) returning;
        } catch (Exception e) {
            LOGGER.error("could not create audit", e);
        }
    }

    @Override
    public Audit<?, ?> provide() {
        if (toUser == null) {
            LOGGER.error("The to provider of create user is null ");
            return null;
        }
        AuditClientDTO to = new AuditClientDTO();
        to.setUsername(toUser.getUsername());
        to.setName(toUser.getName());
        to.setClientId(toUser.getClientId());
        to.setEnabled(toUser.isEnabled());
        to.setSuperAdmin(toUser.isSuperAdmin());
        SysLogger.log("Client '" + toUser.getClientId() + "' created successfully by user '" + AuditUtil.getLoggedInUser() + "'");
        return new Audit<>(null, to);
    }
}
