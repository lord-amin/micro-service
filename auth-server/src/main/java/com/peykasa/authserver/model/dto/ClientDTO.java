package com.peykasa.authserver.model.dto;

import lombok.Data;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
public class ClientDTO extends BaseDTO {

    private String clientId;

    private String clientSecret;
    private String grantType;

    private Integer accessTokenValiditySeconds = 10 * 60;
    private Integer refreshTokenValiditySeconds = 24 * 60 * 60;

}
