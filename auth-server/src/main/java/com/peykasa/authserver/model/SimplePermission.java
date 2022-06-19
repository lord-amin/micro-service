package com.peykasa.authserver.model;

import com.peykasa.authserver.model.dto.SimpleDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@NoArgsConstructor
public class SimplePermission extends SimpleDTO {
    private Long id;
    private String title;
    private String permission;

    public SimplePermission(String d) {
        super(d);
    }

    public SimplePermission(Long id, String permission) {
        this.id = id;
        this.permission = permission;
    }


}
