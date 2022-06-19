package com.peykasa.authserver.audit.aspect.provider.login;

import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.tools.SysLogger;
import lombok.var;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class LoginProvider extends AuditProvider {


    public LoginProvider(Object returning, Object[] params) {
        super(returning, params);
    }

    @Override
    public Audit<?, Map<String, Object>> provide() {
        var param = (Authentication) getReturning();
        var principal = (User) param.getPrincipal();
        var map = new HashMap<String, Object>();
        map.put("username", principal.getUsername());
        map.put("passwordExpired", principal.isAccountExpired());
        var objectMapAudit = new Audit<Object, Map<String, Object>>(null, map);
        objectMapAudit.setActor(((User) param.getPrincipal()).getUsername());
        SysLogger.log("User '" + principal.getUsername() + "' Logged in successfully");
        return objectMapAudit;
    }
}
