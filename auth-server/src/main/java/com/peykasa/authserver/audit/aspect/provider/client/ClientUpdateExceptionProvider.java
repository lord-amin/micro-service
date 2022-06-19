package com.peykasa.authserver.audit.aspect.provider.client;

import com.peykasa.authserver.audit.aspect.provider.ExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditClientDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.exception.GlobalException;
import com.peykasa.authserver.exception.PermissionDeniedException;
import com.peykasa.authserver.model.cto.UpdateClientCTO;
import lombok.var;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class ClientUpdateExceptionProvider extends ExceptionProvider {
    public ClientUpdateExceptionProvider(Object[] params, Throwable t) {
        super(params, t);
        classes.add(PermissionDeniedException.class);
    }

    @Override
    public Audit<AuditClientDTO, AbstractMap.SimpleEntry<String, String>> provide() {
        var userCTO = (UpdateClientCTO) params[0];
        if (classes.contains(t.getClass())) {
            var messages = ((GlobalException) t).getMessages();
            var from = new AuditClientDTO();
            from.setName(userCTO.getName());
            from.setClientId(userCTO.getClientId());
            from.setEnabled(userCTO.getEnabled());
            return new Audit<>(from, new AbstractMap.SimpleEntry<>("msg", messages.get(0)));
        }
        return null;
    }
}
