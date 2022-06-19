package com.peykasa.audit.exception;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
@NoArgsConstructor
public class RestErrorResponse {
    private String key;
    private List<String> messages = new ArrayList<>();

    public RestErrorResponse(String key, List<String> messages) {
        this.key = key;
        this.messages.addAll(messages);
    }

    public RestErrorResponse(String key, String message) {
        this.key=key;
        this.messages.add(message);
    }

    public String getKey() {
        return key;
    }

    public List<String> getMessages() {
        return messages;
    }
}
