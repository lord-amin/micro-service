package com.peykasa.authserver.controller.validator;

import com.peykasa.authserver.model.cto.CreateClientCTO;
import com.peykasa.authserver.model.cto.UpdateClientCTO;
import com.peykasa.authserver.validation.PasswordRules;
import com.peykasa.authserver.validation.ValidationConfiguration;
import com.peykasa.authserver.validation.ValidationContext;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Collections;

import static com.peykasa.authserver.Constants.*;

@Service
public class UserClientValidator {
    @Getter
    private ValidationContext<CreateClientCTO> createUserClientValidator = new ValidationContext<>();
    @Getter
    private ValidationContext<UpdateClientCTO> updateUserClientValidator = new ValidationContext<>();
    private ValidationConfiguration validationConfiguration;

    public UserClientValidator(ValidationConfiguration validationConfiguration) {
        this.validationConfiguration = validationConfiguration;
        initCreate();
        initUpdate();
    }

    private void initCreate() {
        this.createUserClientValidator.addValidation(userCTO -> StringUtils.isEmpty(StringUtils.trim(userCTO.getName())) ? Collections.singletonList(MessageFormat.format(IS_NULL, "Name")) : null);
        this.createUserClientValidator.addValidation(createClientCTO -> {
            if (createClientCTO.getName().length() > 255)
                return Collections.singletonList(MessageFormat.format(MAX_LENGTH, "Name", 255));
            return null;
        });
        this.createUserClientValidator.addValidation(userCTO -> StringUtils.isEmpty(StringUtils.trim(userCTO.getUsername())) ? Collections.singletonList(USER_NAME_NULL) : null);
        this.createUserClientValidator.addValidation(userCTO -> (userCTO.getUsername()).contains(" ") ? Collections.singletonList(SPACE_NOT) : null);
        this.createUserClientValidator.addValidation(userCTO -> StringUtils.isEmpty(userCTO.getPassword()) ? Collections.singletonList(PASSWORD_NULL) : null);
        this.createUserClientValidator.addValidation(userCTO -> new PasswordRules().validate(userCTO.getPassword(), validationConfiguration.getValidation()));
        this.createUserClientValidator.addValidation(createUserClientCTO -> {
            if (StringUtils.isEmpty(createUserClientCTO.getClientId())) {
                return Collections.singletonList(MessageFormat.format(IS_NULL, createUserClientCTO.getClientId()));
            }
            return null;
        });
        this.createUserClientValidator.addValidation(createUserClientCTO -> {
            if (StringUtils.isEmpty(createUserClientCTO.getConfirmPassword()) || !createUserClientCTO.getPassword().equals(createUserClientCTO.getConfirmPassword())) {
                return Collections.singletonList(PASSWORD_MSG);
            }
            return null;
        });
    }

    private void initUpdate() {
        this.updateUserClientValidator.addValidation(updateClientCTO -> StringUtils.isEmpty(StringUtils.trim(updateClientCTO.getClientId())) ? Collections.singletonList(MessageFormat.format(IS_NULL, "ClientId")) : null);
        this.updateUserClientValidator.addValidation(updateClientCTO -> {
            if (updateClientCTO.getName() == null)
                return null;
            return StringUtils.isBlank(StringUtils.trim(updateClientCTO.getName())) ? Collections.singletonList(MessageFormat.format(IS_BLANK, "Name")) : null;
        });
        this.updateUserClientValidator.addValidation(createClientCTO -> {
            if (createClientCTO.getName() == null)
                return null;
            if (createClientCTO.getName().length() > 255)
                return Collections.singletonList(MessageFormat.format(MAX_LENGTH, "Name", 255));
            return null;
        });

        this.updateUserClientValidator.addValidation(updateClientCTO -> {
            if (StringUtils.isEmpty(updateClientCTO.getNewPassword()))
                return null;
            if (!updateClientCTO.getNewPassword().equals(updateClientCTO.getConfirmPassword()))
                return Collections.singletonList(PASSWORD_MSG);
            return new PasswordRules().validate(updateClientCTO.getNewPassword(), validationConfiguration.getValidation());
        });
    }
}
