package com.peykasa.authserver.model.dto;

import com.peykasa.authserver.model.SimpleRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@NoArgsConstructor
public class RoleDTO extends SimpleRole {
    private String description;
    private List<PermissionDTO> permissions ;

    public RoleDTO(String d) {
        super(d);
    }


}
