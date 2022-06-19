package com.peykasa.authserver.audit.aspect.provider.User;

import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.tools.SysLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class UserChangePassProvider extends AuditProvider {

    @Autowired
    private UserRepository userRepository;

    public UserChangePassProvider(Object returning, Object[] params) {
        super(returning, params);
    }

    @Override
    public Audit<?, ?> provide() {
        String log = "User '" + AuditUtil.getUser(userRepository, getParams()[0]) + "' password changed successfully by user '" + AuditUtil.getLoggedInUser() + "'";
        SysLogger.log(log);
        return new Audit<>(null, new AbstractMap.SimpleEntry<>("msg", log + " : " + log));
    }
}
