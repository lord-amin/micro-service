package com.peykasa.silo.central.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @author Kamran Ghiasvand
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class UserEntity {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean enabled = true;
    private Date passExpirationDate;
    private Date lastLoginAttemptTime;
    private Date blockDate;
    private Integer blockCount;
    private Boolean superAdmin = false;
    private boolean deleted = false;
    private Date creationDate;
    private Date modifiedDate;

}
