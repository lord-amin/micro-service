package com.peykasa.authserver.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @author Yaser(amin) Sadeghi
 */
@Getter
@Setter
@NoArgsConstructor
public class UserClientDTO {
    private String name;
    private String username;
    private String clientId;
    private Date creationDate;
    private boolean enabled;
    private boolean superAdmin;
}
