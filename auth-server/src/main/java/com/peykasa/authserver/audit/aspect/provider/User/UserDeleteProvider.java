package com.peykasa.authserver.audit.aspect.provider.User;

import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.tools.SysLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class UserDeleteProvider extends AuditProvider {
    @Autowired
    protected UserRepository userRepository;


    public UserDeleteProvider(Object returning, Object[] params) {
        super(returning, params);
    }

    @Override
    public Audit<Map<String, Serializable>, ?> provide() {
        Long param = (Long) getParams()[0];
        SysLogger.log("User '" + AuditUtil.getUser(userRepository, param) + "' deleted successfully by user '" + AuditUtil.getLoggedInUser() + "'");
        Map<String, Serializable> map = new HashMap<>();
        map.put("id", param);
        map.put("username", AuditUtil.getUser(userRepository, param));
        return new Audit<>(map, null);

    }
}
