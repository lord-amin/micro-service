package com.peykasa.authserver.audit.aspect.provider.Role;

import com.peykasa.authserver.audit.aspect.provider.dto.AuditRoleDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import lombok.var;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class RoleUpdateExceptionProvider extends RoleCreateExceptionProvider {

    public RoleUpdateExceptionProvider(Object[] params, Throwable t) {
        super(params, t);
    }

    @Override
    public Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>> provide() {
        var provide = super.provide();
        if (provide != null) {
            AuditRoleDTO fromState = provide.getFromState();
            fromState.setId((Long) params[1]);
        }
        return provide;

    }
}
