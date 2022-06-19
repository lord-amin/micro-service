package com.peykasa.authserver.exception;

import com.peykasa.authserver.Constants;
import org.springframework.http.HttpStatus;

/**
 * @author Yaser(amin) Sadeghi
 */
public class DuplicateResourceException extends GlobalException {

    public DuplicateResourceException(Throwable t, String message) {
        super(t, message);
        key = Constants.DUPLICATE_ERROR;
        status = HttpStatus.CONFLICT;
    }
}
