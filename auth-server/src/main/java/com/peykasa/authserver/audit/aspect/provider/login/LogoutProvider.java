package com.peykasa.authserver.audit.aspect.provider.login;

import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.tools.SysLogger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class LogoutProvider extends AuditProvider {


    public LogoutProvider(Object returning, Object[] params) {
        super(returning, params);
    }

    @Override
    public Audit<?, Map<String, Object>> provide() {
        String loggedInUser = AuditUtil.getLoggedInUser();
        SysLogger.log("User '" + loggedInUser + "' Logged out successfully");
        return new Audit<>(null, null);
    }
}
