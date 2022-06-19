package com.peykasa.authserver.audit.aspect.provider.login;

import com.peykasa.authserver.Constants;
import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.aspect.provider.ExceptionProvider;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.tools.SysLogger;
import lombok.var;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class LoginExceptionProvider extends ExceptionProvider {

    public LoginExceptionProvider(Object[] params, Throwable t) {
        super(params, t);
        classes.clear();
        classes.add(BadCredentialsException.class);
        classes.add(LockedException.class);
        classes.add(DisabledException.class);
    }

    @Override
    public Audit<?, AbstractMap.SimpleEntry<String, String>> provide() {
        var param = (Authentication) params[0];
        var audit = new Audit<>(new AbstractMap.SimpleEntry<>("username", param.getPrincipal()),
                new AbstractMap.SimpleEntry<>("msg", t.getMessage()));
        if (t.getClass().equals(UsernameNotFoundException.class)) {
            audit.setActor(Constants.INVALID_USER);
            SysLogger.log("Login failed , Bad Credential for user '" + param.getPrincipal()+"'");
            return audit;
        } else if (classes.contains(t.getClass())) {
            audit.setActor(param.getPrincipal().toString());
            SysLogger.log("Login failed , Bad Credential for user '" + param.getPrincipal()+"'");
            return audit;
        }
        return null;
    }
}
