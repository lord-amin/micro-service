package com.peykasa.authserver.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@NoArgsConstructor
public class SimpleDTO extends BaseDTO {
    @SuppressWarnings("all")
    public SimpleDTO(Long id) {
        super(id);
    }

    public SimpleDTO(String d) {
        String[] split = d.split("/");
        setId(Long.valueOf(split[split.length - 1]));
    }

}
