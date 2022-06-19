package com.peykasa.authserver.audit.aspect.provider.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchInputDTO {
    private PageCTO page;
    private String sort;

    public SearchInputDTO(String page, String size, String sort) {
        try {
            this.page = new PageCTO(Integer.valueOf(page),Integer.valueOf(size));
        } catch (NumberFormatException ignored) {
        }
        this.sort = sort;
    }
}