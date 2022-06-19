package com.peykasa.authserver.exception;

import com.peykasa.authserver.Constants;
import org.springframework.http.HttpStatus;

/**
 * @author Yaser(amin) Sadeghi
 */
public class UnprocessableException extends GlobalException {

    public UnprocessableException(Throwable t, String message) {
        super(t, message);
        key = Constants.RELATION_ERROR;
        status = HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
