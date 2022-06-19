package com.peykasa.authserver.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Yaser(amin) Sadeghi
 */
@Setter
@Getter
public class MePassword extends Password {
    private String oldPassword;

}
