package com.peykasa.authserver.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

import static com.peykasa.authserver.Constants.VALIDATION_ERROR;

/**
 * @author Yaser(amin) Sadeghi
 */
public class ValidationException extends GlobalException {

    public ValidationException(List<String> message) {
        super(message);
        key = VALIDATION_ERROR;
        status = HttpStatus.BAD_REQUEST;
    }

    public ValidationException(String message) {
        super(null, message);
        key = VALIDATION_ERROR;
        status = HttpStatus.BAD_REQUEST;
    }
}
