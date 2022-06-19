package com.peykasa.authserver.audit.aspect.provider.User;

import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditUserDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.model.SimpleRole;
import com.peykasa.authserver.model.dto.UserDTO;
import lombok.var;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.stream.Collectors;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class UserGetProviderUser extends AuditProvider {

    private AuditUserDTO user;

    public UserGetProviderUser(Object returning, Object[] params) {
        super(returning, params);
        var userDTO = (UserDTO) returning;
        user = new AuditUserDTO();
        user.setId(userDTO.getId());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setUsername(userDTO.getUsername());
        user.setRoles(userDTO.getRoles().stream().map(SimpleRole::getName).collect(Collectors.toList()));
    }

    @Override
    public Audit<AbstractMap.SimpleEntry<String, Object>, AuditUserDTO> provide() {
        return new Audit<>(new AbstractMap.SimpleEntry<>("id", getParams()[0]), user);
    }
}
