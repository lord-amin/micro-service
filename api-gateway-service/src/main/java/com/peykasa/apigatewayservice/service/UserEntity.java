package com.peykasa.apigatewayservice.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

/**
 * @author Kamran Ghiasvand
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class UserEntity {
    private String email;
    private String uuid;
    private String firstName;
    private String lastName;
}
