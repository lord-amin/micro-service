package com.peykasa.authserver.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Yaser(amin) Sadeghi
 */
@Getter
@Setter
public abstract class Password {
    private String newPassword;
    private String confirmPassword;
}
