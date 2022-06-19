package com.peykasa.authserver.model.cto;

import com.peykasa.authserver.utility.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @author kamran
 */
@Getter
@Setter
@NoArgsConstructor
public class ExtendedUserCTO extends CreateUserCTO {
    private Boolean enabled;
    private Date creationDate;
    private Date blockDate;

    public ExtendedUserCTO copy() {
        ExtendedUserCTO copy = new ExtendedUserCTO();
        ObjectMapper.copy(this, copy);
        return copy;
    }
}
