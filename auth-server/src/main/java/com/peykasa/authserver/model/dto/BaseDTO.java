package com.peykasa.authserver.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@NoArgsConstructor
public abstract class BaseDTO {
    private Long id;

    public BaseDTO(Long id) {
        this.id = id;
    }
}
