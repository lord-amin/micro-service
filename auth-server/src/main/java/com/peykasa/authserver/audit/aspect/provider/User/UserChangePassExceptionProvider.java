package com.peykasa.authserver.audit.aspect.provider.User;

import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.aspect.provider.ExceptionProvider;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.exception.GlobalException;
import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.tools.SysLogger;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class UserChangePassExceptionProvider extends ExceptionProvider {
    @Autowired
    private UserRepository userService;

    public UserChangePassExceptionProvider(Object[] params, Throwable t) {
        super(params, t);
    }

    @Override
    public Audit<?, AbstractMap.SimpleEntry<String, String>> provide() {

        if (classes.contains(t.getClass())) {
            var messages = ((GlobalException) t).getMessages();
            String log = "Changing user '" + AuditUtil.getUser(userService, params[0]) + "' password failed by user '" + AuditUtil.getLoggedInUser() + "'";
            SysLogger.log(log);
            return new Audit<>(null, new AbstractMap.SimpleEntry<>("msg", log + " : " + messages.get(0)));
        }
        return null;
    }

}
