package com.peykasa.authserver.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
public class RestErrorResponse {
    private String key;
    private List<String> messages = new ArrayList<>();

    public RestErrorResponse() {
    }

    public RestErrorResponse(String key, List<String> messages) {
        this();
        this.key = key;
        this.messages.addAll(messages);
    }

    public RestErrorResponse(String key, String message) {
        this(key, new ArrayList<String>() {{
            add(message);
        }});
    }

    public String getKey() {
        return key;
    }

    public List<String> getMessages() {
        return messages;
    }
}
