package com.peykasa.authserver.audit.aspect.provider.login;

import com.peykasa.authserver.Constants;
import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.aspect.provider.ExceptionProvider;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.exception.ResourceNotFoundException;
import com.peykasa.authserver.tools.SysLogger;
import lombok.var;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class LogoutExceptionProvider extends ExceptionProvider {

    public LogoutExceptionProvider(Object[] params, Throwable t) {
        super(params, t);
        classes.clear();
        classes.add(ResourceNotFoundException.class);
    }

    @Override
    public Audit<?, AbstractMap.SimpleEntry<String, String>> provide() {
        String loggedInUser = AuditUtil.getLoggedInUser();
        var audit = new Audit<>(new AbstractMap.SimpleEntry<>("username", loggedInUser),
                new AbstractMap.SimpleEntry<>("msg", t.getMessage()));
        if (t.getClass().equals(UsernameNotFoundException.class)) {
            audit.setActor(Constants.INVALID_USER);
            SysLogger.log("Logout failed , Bad Credential for user '" + loggedInUser + "'");
            return audit;

        }
        return null;
    }
}
