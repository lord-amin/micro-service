package com.peykasa.authserver.audit.aspect.provider.Role;


import com.peykasa.authserver.audit.aspect.provider.dto.AuditRoleDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.model.SimplePermission;
import com.peykasa.authserver.model.cto.RoleCTO;
import com.peykasa.authserver.model.dto.PermissionDTO;
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
public class RoleUpdateProvider extends RoleProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleUpdateProvider.class);
    private RoleCTO fromRole;

    public RoleUpdateProvider(Object returning, Object[] params) {
        super(returning, params);
        fromRole = (RoleCTO) params[0];
    }

    @Override
    public Audit<AuditRoleDTO, AuditRoleDTO> provide() {
        var id = (Long) getParams()[1];
        if (toRole == null) {
            LOGGER.error("The to provider of create user is null ");
            return null;
        }
        var from = new AuditRoleDTO();
        var to = new AuditRoleDTO();
        // audit for update role
        if (fromRole.getName() != null) {
            if (!fromRole.getName().equals(toRole.getName())) {
                to.setName(toRole.getName());
                from.setName(fromRole.getName());
            }
        }
        if (fromRole.getDescription() != null) {
            if (!fromRole.getDescription().equals(toRole.getDescription())) {
                to.setDesc(toRole.getDescription());
                from.setDesc(fromRole.getDescription());
            }
        }
        if (fromRole.getPermissions() != null) {
            fromRole.getPermissions().sort(Comparator.comparing(SimplePermission::getPermission));
            var pFrom = fromRole.getPermissions().stream().map(SimplePermission::getPermission).collect(Collectors.toList());
            var permsTo = toRole.getPermissions().stream().map(PermissionDTO::getPermission).sorted().collect(Collectors.toList());

            if (!pFrom.toString().equals(permsTo.toString())) {
                to.setPermissions(new ArrayList<>());
                from.setPermissions(new ArrayList<>());
                to.getPermissions().addAll(permsTo);
                from.getPermissions().addAll(pFrom);
            }
        }
        if (id != null) {
            from.setId(id);
            to.setId(id);
        }
        return new Audit<>(from, to);
    }
}
