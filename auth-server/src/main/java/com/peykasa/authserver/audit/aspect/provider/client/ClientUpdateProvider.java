package com.peykasa.authserver.audit.aspect.provider.client;

import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditClientDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.model.cto.UpdateClientCTO;
import com.peykasa.authserver.model.dto.UserClientDTO;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class ClientUpdateProvider extends AuditProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientUpdateProvider.class);
    private UpdateClientCTO fromUser;
    protected UserClientDTO toUser;

    public ClientUpdateProvider(Object returning, Object[] params) {
        super(returning, params);
        try {
            toUser = (UserClientDTO) returning;
        } catch (Exception e) {
            LOGGER.error("could not create audit", e);
        }
        fromUser = (UpdateClientCTO) params[0];
    }

    @Override
    public Audit<AuditClientDTO, AuditClientDTO> provide() {
        if (toUser == null) {
            LOGGER.error("The to provider of create user is null ");
            return null;
        }
        var from = new AuditClientDTO();
        var to = new AuditClientDTO();
        // audit for update user
        if (fromUser.getName() != null) {
            if (!fromUser.getName().equals(toUser.getName())) {
                to.setName(toUser.getName());
                from.setName(fromUser.getName());
            }
        }
        if (fromUser.getEnabled() != null) {
            if (!fromUser.getEnabled().equals(toUser.isEnabled())) {
                to.setEnabled(toUser.isEnabled());
                from.setEnabled(fromUser.getEnabled());
            }
        }
        return new Audit<>(from, to);
    }
}
