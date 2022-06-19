package com.peykasa.authserver.model.cto;

import com.peykasa.authserver.model.SimplePermission;
import com.peykasa.authserver.utility.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@NoArgsConstructor
public class RoleCTO extends BaseCTO {
    private String name;
    private String description;
    private List<SimplePermission> permissions;

    public RoleCTO copy() {
        RoleCTO copy = new RoleCTO();
        ObjectMapper.copy(this, copy);
        return copy;
    }
}
