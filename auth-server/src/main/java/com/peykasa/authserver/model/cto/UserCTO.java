package com.peykasa.authserver.model.cto;

import com.peykasa.authserver.model.SimpleRole;
import com.peykasa.authserver.utility.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
@Getter
@Setter
@NoArgsConstructor
public class UserCTO extends BaseCTO {
    private String firstName;
    private String lastName;
    private List<SimpleRole> roles;

    public UserCTO copy() {
        UserCTO copy = new UserCTO();
        ObjectMapper.copy(this, copy);
        return copy;
    }
}
