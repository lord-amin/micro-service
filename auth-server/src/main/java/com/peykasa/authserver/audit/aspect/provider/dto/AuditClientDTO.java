package com.peykasa.authserver.audit.aspect.provider.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuditClientDTO {
    private String name;
    private String username;
    private String clientId;
    private boolean enabled;
    private boolean superAdmin;
}