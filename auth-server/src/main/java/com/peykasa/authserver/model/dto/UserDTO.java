package com.peykasa.authserver.model.dto;

import com.peykasa.authserver.model.SimpleRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@NoArgsConstructor
public class UserDTO extends BaseDTO {
    private String firstName;
    private String lastName;
    private String username;
    private Date creationDate;
    private boolean enabled = true;
    private Date passExpirationDate;
    private Date lastLoginAttemptTime;
    private Date blockDate;
    private boolean superAdmin=false;
    private List<SimpleRole> roles = new ArrayList<>();

}
