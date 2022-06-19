package com.peykasa.authserver.audit.aspect.provider.User;

import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.model.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yaser(amin) Sadeghi
 */
public abstract class UserAuditProvider extends AuditProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserAuditProvider.class);
    protected UserDTO toUser;

    public UserAuditProvider(Object returning, Object[] params) {
        super(returning, params);
        try {
            toUser = (UserDTO) returning;
        } catch (Exception e) {
            LOGGER.error("could not create audit", e);
        }
    }
}
