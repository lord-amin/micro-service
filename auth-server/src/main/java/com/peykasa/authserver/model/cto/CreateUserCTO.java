package com.peykasa.authserver.model.cto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Yaser(amin) Sadeghi
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateUserCTO extends UserCTO {
    private String password;
    private String username;
    private Long id;
    private boolean enabled = true;

}
