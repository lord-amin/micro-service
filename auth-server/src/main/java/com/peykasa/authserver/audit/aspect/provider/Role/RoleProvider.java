package com.peykasa.authserver.audit.aspect.provider.Role;

import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.model.dto.RoleDTO;
import org.springframework.stereotype.Component;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
public abstract class RoleProvider extends AuditProvider {
    protected RoleDTO toRole;

    public RoleProvider(Object returning, Object[] params) {
        super(returning, params);
        toRole = (RoleDTO) returning;
    }


}
