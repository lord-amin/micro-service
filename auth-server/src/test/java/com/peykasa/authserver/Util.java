package com.peykasa.authserver;

import com.peykasa.authserver.validation.ValidationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
public class Util {
    @Autowired
    private ValidationConfiguration validationConfiguration;

    public void resetValidation() {
        validationConfiguration.getValidation().setHasDigit(false);
        validationConfiguration.getValidation().setHasUpper(false);
        validationConfiguration.getValidation().setHasLower(false);
        validationConfiguration.getValidation().setHasSpecial(false);
        validationConfiguration.getValidation().setMinLength(-1);
        validationConfiguration.getValidation().setMaxLength(-1);
    }

}
