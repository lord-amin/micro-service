package com.peykasa.authserver.model;

import com.peykasa.authserver.model.dto.BaseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class SimpleRole extends BaseDTO {

    private String name;

    @SuppressWarnings("all")
    public SimpleRole(Long id) {
        super(id);
    }

    public SimpleRole(Long id, String name) {
        super(id);
        this.name = name;
    }

    public SimpleRole(String d) {
        String[] split = d.split("/");
        setId(Long.valueOf(split[split.length - 1]));
    }
}