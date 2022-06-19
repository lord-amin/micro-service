package com.peykasa.authserver.audit.aspect.provider.client;

import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.aspect.provider.ExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditClientDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.exception.GlobalException;
import com.peykasa.authserver.model.cto.CreateClientCTO;
import com.peykasa.authserver.tools.SysLogger;
import lombok.var;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class ClientCreateExceptionProvider extends ExceptionProvider {

    public ClientCreateExceptionProvider(Object[] params, Throwable t) {
        super(params, t);
    }

    @Override
    public Audit<AuditClientDTO, AbstractMap.SimpleEntry<String, String>> provide() {
        var userCTO = (CreateClientCTO) params[0];
        if (classes.contains(t.getClass())) {
            var messages = ((GlobalException) t).getMessages();
            var from = new AuditClientDTO();
            from.setClientId(userCTO.getClientId());
            from.setName(userCTO.getName());
            from.setUsername(userCTO.getUsername());
            from.setEnabled(true);
            from.setSuperAdmin(true);
            String log = "Creating Client '" + userCTO.getClientId() + "' failed by user '" + AuditUtil.getLoggedInUser() + "'";
            SysLogger.log(log);
            return new Audit<>(from, new AbstractMap.SimpleEntry<>("msg", log + " : " + messages.get(0)));
        }
        return null;
    }
}
