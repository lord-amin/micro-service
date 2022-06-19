package com.peykasa.authserver.validation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.validation.Validator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Yaser(amin) Sadeghi
 */
@Configuration
public class ValidatorEventRegister implements InitializingBean {

    private final ValidatingRepositoryEventListener validatingRepositoryEventListener;

    private final Map<String, Validator> validators;

    @Autowired
    public ValidatorEventRegister(Map<String, Validator> validators, ValidatingRepositoryEventListener validatingRepositoryEventListener) {
        this.validators = validators;
        this.validatingRepositoryEventListener = validatingRepositoryEventListener;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> events = Collections.singletonList("beforeCreate");
        for (Map.Entry<String, Validator> entry : validators.entrySet()) {
            events.stream()
                    .filter(p -> entry.getKey().startsWith(p))
                    .findFirst()
                    .ifPresent(
                            p -> validatingRepositoryEventListener
                                    .addValidator(p, entry.getValue()));
        }
    }

//
}