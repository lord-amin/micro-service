package com.peykasa.authserver.exception;

import com.peykasa.authserver.Constants;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * @author kamran
 */
public class PermissionDeniedException extends GlobalException {
    public PermissionDeniedException(String messages) {
        super(messages);
        status= HttpStatus.FORBIDDEN;
        key= Constants.PERMISSION_DENIED_ERROR;
    }
}
