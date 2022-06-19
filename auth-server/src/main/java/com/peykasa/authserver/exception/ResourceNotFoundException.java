package com.peykasa.authserver.exception;

import com.peykasa.authserver.Constants;
import org.springframework.http.HttpStatus;

/**
 * @author Yaser(amin) Sadeghi
 */
public class ResourceNotFoundException extends GlobalException {
    public ResourceNotFoundException(Throwable t, String message) {
        super(t, message);
        key = Constants.NOTE_FOUND_ERROR;
        status = HttpStatus.NOT_FOUND;
    }
}
