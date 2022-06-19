package com.peykasa.apigatewayservice;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kamran ghiasvand
 */
@ToString
@NoArgsConstructor
@Data
public class RestErrorResponse {
    private String key;
    private List<Object> messages = new ArrayList<>();

    public RestErrorResponse(String key, List<?> messages) {
        this.key = key;
        this.messages.addAll(messages);
    }

    public RestErrorResponse(String key, String message) {
        this.key = key;
        this.messages.add(message);
    }
}
