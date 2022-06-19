package com.peykasa.authserver.audit.aspect.provider;

import com.peykasa.authserver.audit.aspect.AuditUtil;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.repository.UserRepository;
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
final public class DeleteProvider extends AuditProvider {
    @Autowired
    protected UserRepository userRepository;

    public DeleteProvider(Object returning, Object[] params) {
        super(returning, params);
    }

    @Override
    public Audit<Map<String, Serializable>, ?> provide() {
        Map<String, Serializable> map = new HashMap<>();
        Long param = (Long) getParams()[0];
        map.put("id", param);
        map.put("username", AuditUtil.getUser(userRepository, param));
        return new Audit<>(map, null);
    }
}
