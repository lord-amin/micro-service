package com.peykasa.authserver.model.dto;

import com.peykasa.authserver.model.SimplePermission;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@NoArgsConstructor
public class PermissionDTO extends SimplePermission {
    private List<PermissionDTO> children = new ArrayList<>();

    public PermissionDTO(String d) {
        super(d);
    }

}
