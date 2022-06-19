package com.peykasa.authserver.audit.aspect.provider;

import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.exception.GlobalException;
import com.peykasa.authserver.exception.PermissionDeniedException;
import com.peykasa.authserver.repository.UserRepository;
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
public class DeleteExceptionProvider extends ExceptionProvider {
    @Autowired
    protected UserRepository userRepository;

    public DeleteExceptionProvider(Object[] params, Throwable t) {
        super(params, t);
        classes.add(PermissionDeniedException.class);
    }

    @Override
    public Audit<?, AbstractMap.SimpleEntry<String, String>> provide() {
        if (classes.contains(t.getClass())) {
            var messages = ((GlobalException) t).getMessages();
            String log = "User " + AuditUtil.getUser(userRepository, params[0]) + " delete failed by user " + AuditUtil.getLoggedInUser();
            return new Audit<>(new AbstractMap.SimpleEntry<>("id", params[0]), new AbstractMap.SimpleEntry<>("msg", log + " : " + messages.get(0)));
        }
        return null;
    }
}
