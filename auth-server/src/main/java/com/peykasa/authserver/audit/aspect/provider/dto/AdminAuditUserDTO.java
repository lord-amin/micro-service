package com.peykasa.authserver.audit.aspect.provider.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AdminAuditUserDTO extends AuditUserDTO {
    private Date creationDate;
    private Date blockDate;
}