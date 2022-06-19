package com.peykasa.authserver.controller;

import com.peykasa.authserver.Constants;
import com.peykasa.authserver.exception.GlobalException;
import com.peykasa.authserver.exception.RestErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.peykasa.authserver.Constants.UNHANDLED_ERROR;
import static com.peykasa.authserver.Constants.VALIDATION_ERROR;

/**
 * @author Yaser(amin) Sadeghi
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    protected final static Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<RestErrorResponse> handleOthers(Throwable ex) {
        LOGGER.error(ex.getMessage(), ex);
        return new ResponseEntity<>(new RestErrorResponse(UNHANDLED_ERROR, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({GlobalException.class})
    public ResponseEntity<RestErrorResponse> handleNFSE(GlobalException ex) {
        LOGGER.error(ex.getMessages().get(0), ex);
        return new ResponseEntity<>(new RestErrorResponse(ex.getKey(), ex.getMessages()), ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        LOGGER.error(ex.getMessage(), ex);
        List<String> errorList = new ArrayList<>();
        List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();
        if (!allErrors.isEmpty()) {
            allErrors.forEach(err -> errorList.add(err.getDefaultMessage()));
        }
        RestErrorResponse restErrorResponse = new RestErrorResponse(VALIDATION_ERROR, errorList);
        return new ResponseEntity<>(restErrorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RestErrorResponse> handleException(MethodArgumentTypeMismatchException exception) {
        LOGGER.error(exception.getMessage(), exception);
        ArrayList<String> messages = new ArrayList<>();
        messages.add(MessageFormat.format("{0} is not valid for {1}", exception.getValue(), exception.getName()));
        return new ResponseEntity<>(new RestErrorResponse(Constants.VALIDATION_ERROR, messages), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RestErrorResponse> handleException(HttpMessageNotReadableException exception) {
        LOGGER.error(exception.getMessage(), exception);
        ArrayList<String> messages = new ArrayList<>();
        messages.add(exception.getMessage());
        return new ResponseEntity<>(new RestErrorResponse(Constants.CLIENT_ERROR, messages), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RestErrorResponse> illegalArgumentException(IllegalArgumentException ex) {
        LOGGER.error(ex.getMessage(), ex);
        RestErrorResponse restErrorResponse = new RestErrorResponse(Constants.CLIENT_ERROR, ex.getMessage());
        return new ResponseEntity<>(restErrorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

}