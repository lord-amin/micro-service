package com.peykasa.authserver.audit.aspect.provider.User;

import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.aspect.provider.ExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditUserDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.exception.GlobalException;
import com.peykasa.authserver.model.cto.CreateUserCTO;
import com.peykasa.authserver.model.dto.BaseDTO;
import com.peykasa.authserver.tools.SysLogger;
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
public class UserCreateExceptionProvider extends ExceptionProvider {

    public UserCreateExceptionProvider(Object[] params, Throwable t) {
        super(params, t);
    }

    @Override
    public Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> provide() {
        var userCTO = (CreateUserCTO) params[0];
        if (classes.contains(t.getClass())) {
            var messages = ((GlobalException) t).getMessages();
            var from = new AuditUserDTO();
            from.setUsername(userCTO.getUsername());
            from.setFirstName(userCTO.getFirstName());
            from.setLastName(userCTO.getLastName());
            if (userCTO.getRoles() != null)
                from.setRoles(userCTO.getRoles().stream().map(BaseDTO::getId).sorted().collect(Collectors.toList()));
            String log = "Creating User '" + userCTO.getUsername() + "' failed by user '" + AuditUtil.getLoggedInUser()+"'";
            SysLogger.log(log);
            return new Audit<>(from, new AbstractMap.SimpleEntry<>("msg", log + " : " + messages.get(0)));
        }
        return null;
    }
}
