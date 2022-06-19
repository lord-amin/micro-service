package com.peykasa.authserver.audit.aspect.provider.client;

import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditClientDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.model.dto.UserClientDTO;
import lombok.var;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class ClientGetProviderUser extends AuditProvider {

    private AuditClientDTO user;

    public ClientGetProviderUser(Object returning, Object[] params) {
        super(returning, params);
        var userClientDTO = (UserClientDTO) returning;
        user = new AuditClientDTO();
        user.setClientId(userClientDTO.getClientId());
        user.setName(userClientDTO.getName());
        user.setUsername(userClientDTO.getUsername());
        user.setEnabled(userClientDTO.isEnabled());
        user.setSuperAdmin(userClientDTO.isSuperAdmin());
    }

    @Override
    public Audit<AbstractMap.SimpleEntry<String, Object>, AuditClientDTO> provide() {
        return new Audit<>(new AbstractMap.SimpleEntry<>("clientId", getParams()[0]), user);
    }
}
