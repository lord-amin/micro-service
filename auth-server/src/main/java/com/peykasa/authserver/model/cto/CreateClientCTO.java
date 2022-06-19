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
public class CreateClientCTO extends BaseCTO {
    private String name;
    private String clientId;
    private String username;
    private String password;
    private String confirmPassword;
}
