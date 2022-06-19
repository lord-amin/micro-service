package com.peykasa.authserver.audit.aspect.provider.Role;


import com.peykasa.authserver.audit.aspect.provider.dto.AuditRoleDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.model.SimplePermission;
import lombok.var;
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
public class RoleCreateProvider extends RoleProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleCreateProvider.class);

    public RoleCreateProvider(Object returning, Object[] params) {
        super(returning, params);
    }

    @Override
    public Audit<?, AuditRoleDTO> provide() {
        if (toRole == null) {
            LOGGER.error("The to provider of create user is null ");
            return null;
        }
        var to = new AuditRoleDTO();
        to.setId(toRole.getId());
        to.setName(toRole.getName());
        to.setDesc(toRole.getDescription());
        if (toRole.getPermissions() != null)
            to.setPermissions(toRole.getPermissions().stream()
                    .map(SimplePermission::getPermission).sorted().collect(Collectors.toList()));
        return new Audit<>(null, to);
    }
}
