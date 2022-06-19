package com.peykasa.authserver.audit.aspect.provider.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class AuditRoleDTO {
    Long id;
    String name;
    String desc;
    List<Serializable> permissions ;
}