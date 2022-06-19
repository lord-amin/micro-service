package com.peykasa.authserver.audit.aspect.provider.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class AuditUserDTO {
    Long id;
    String firstName;
    String lastName;
    String username;
    Boolean enabled;
    List<Serializable> roles;
}