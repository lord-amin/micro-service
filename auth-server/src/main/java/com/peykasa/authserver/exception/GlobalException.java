package com.peykasa.authserver.exception;

import com.peykasa.authserver.Constants;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
public abstract class GlobalException extends Exception {
    String key = Constants.UNHANDLED_ERROR;
    protected HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    private List<String> messages = new ArrayList<>();

    public GlobalException(List<String> messages) {
        this.messages.addAll(messages);
    }

    GlobalException(Throwable t, String message) {
        super(t);
        this.messages.add(message);
    }
    GlobalException(String message){
        this.messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getKey() {
        return key;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
