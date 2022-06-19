package com.peykasa.authserver.audit.aspect.provider.Role;

import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditRoleDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.model.dto.PermissionDTO;
import com.peykasa.authserver.model.dto.RoleDTO;
import lombok.var;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.stream.Collectors;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class RoleGetProvider extends AuditProvider {

    private AuditRoleDTO auditRole;

    public RoleGetProvider(Object returning, Object[] params) {
        super(returning, params);
        var roleDTO = (RoleDTO) returning;
        auditRole = new AuditRoleDTO();
        auditRole.setId(roleDTO.getId());
        auditRole.setName(roleDTO.getName());
        auditRole.setDesc(roleDTO.getDescription());
        if (roleDTO.getPermissions() != null)
            auditRole.setPermissions(roleDTO.getPermissions().stream()
                    .map(PermissionDTO::getPermission).sorted().collect(Collectors.toList()));
    }

    @Override
    public Audit<AbstractMap.SimpleEntry<String, Object>, AuditRoleDTO> provide() {
        return new Audit<>(new AbstractMap.SimpleEntry<>("id", getParams()[0]), auditRole);
    }
}
