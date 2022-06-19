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
public class UpdateClientCTO extends BaseCTO {
    private String clientId;
    private String name;
    private String newPassword;
    private String confirmPassword;
    private Boolean enabled;
}
