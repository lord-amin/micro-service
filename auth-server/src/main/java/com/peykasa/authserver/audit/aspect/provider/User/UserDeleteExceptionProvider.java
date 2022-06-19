package com.peykasa.authserver.audit.aspect.provider.User;

import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.aspect.provider.ExceptionProvider;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.exception.GlobalException;
import com.peykasa.authserver.exception.PermissionDeniedException;
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
public class UserDeleteExceptionProvider extends ExceptionProvider {

    @Autowired
    protected UserRepository userRepository;

    public UserDeleteExceptionProvider(Object[] params, Throwable t) {
        super(params, t);
        classes.add(PermissionDeniedException.class);
    }

    @Override
    public Audit<?, AbstractMap.SimpleEntry<String, String>> provide() {
        Long param = (Long) params[0];
        String log = "Deleting User '" + AuditUtil.getUser(userRepository, param) + "' failed by user '" + AuditUtil.getLoggedInUser()+"'";
        SysLogger.log(log);
        if (classes.contains(t.getClass())) {
            var messages = ((GlobalException) t).getMessages();
            return new Audit<>(new AbstractMap.SimpleEntry<>("id", param), new AbstractMap.SimpleEntry<>("msg", log + " : " + messages.get(0)));
        }
        return null;
    }

}
