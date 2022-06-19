package com.peykasa.authserver.audit.aspect.provider.User;

import com.peykasa.authserver.audit.aspect.provider.ExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditUserDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.exception.GlobalException;
import com.peykasa.authserver.model.cto.UserCTO;
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
public class UserUpdateExceptionProvider extends ExceptionProvider {
    public UserUpdateExceptionProvider(Object[] params, Throwable t) {
        super(params, t);
    }

    @Override
    public Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> provide() {
        var userCTO = (UserCTO) params[0];
        if (classes.contains(t.getClass())) {
            var messages = ((GlobalException) t).getMessages();
            var from = new AuditUserDTO();
            from.setFirstName(userCTO.getFirstName());
            from.setLastName(userCTO.getLastName());
            if (userCTO.getRoles() != null)
                from.setRoles(userCTO.getRoles().stream().map(BaseDTO::getId).sorted().collect(Collectors.toList()));
            return new Audit<>(from, new AbstractMap.SimpleEntry<>("msg", messages.get(0)));
        }
        return null;
    }
}
