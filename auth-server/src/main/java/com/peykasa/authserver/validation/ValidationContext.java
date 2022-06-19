package com.peykasa.authserver.validation;

import com.peykasa.authserver.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
public class ValidationContext<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ValidationContext.class);
    private List<Rule<T>> validations = new ArrayList<>();

    public void validate(T t) throws ValidationException {
        LOGGER.trace("Starting validation");
        for (Rule<T> validate : validations) {
            List<String> validate1 = validate.validate(t);
            if (validate1 != null && !validate1.isEmpty())
                throw new ValidationException(validate1);
        }
    }

    public void addValidation(Rule<T> v) {
        validations.add(v);
    }
}
