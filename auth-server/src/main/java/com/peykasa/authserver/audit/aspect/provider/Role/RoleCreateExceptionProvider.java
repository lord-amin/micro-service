package com.peykasa.authserver.audit.aspect.provider.Role;

import com.peykasa.authserver.audit.aspect.provider.ExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditRoleDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.exception.GlobalException;
import com.peykasa.authserver.model.cto.RoleCTO;
import com.peykasa.authserver.model.dto.BaseDTO;
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
public class RoleCreateExceptionProvider extends ExceptionProvider {

    public RoleCreateExceptionProvider(Object[] params, Throwable t) {
        super(params, t);
    }

    @Override
    public Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>> provide() {
        var roleCTO = (RoleCTO) params[0];
        if (classes.contains(t.getClass())) {
            var messages = ((GlobalException) t).getMessages();
            var from = new AuditRoleDTO();
            from.setName(roleCTO.getName());
            from.setDesc(roleCTO.getDescription());
            if (roleCTO.getPermissions() != null)
                from.setPermissions(roleCTO.getPermissions().stream().map(BaseDTO::getId).sorted().collect(Collectors.toList()));
            return new Audit<>(from, new AbstractMap.SimpleEntry<>("msg", messages.get(0)));
        }
        return null;
    }
}
