package com.peykasa.apigatewayservice.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpMethod;

/**
 * @author Kamran Ghiasvand
 */
@Data
@AllArgsConstructor
public class AuthorizeEntity {
    HttpMethod method;
    String url;
    Boolean permit;
}
