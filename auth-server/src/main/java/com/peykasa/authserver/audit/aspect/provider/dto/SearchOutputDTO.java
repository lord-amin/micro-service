package com.peykasa.authserver.audit.aspect.provider.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchOutputDTO {
    private Long total;
    private Long fetchedCount;

    public SearchOutputDTO(Long total, Integer fetchedCount) {
        this.total = total;
        this.fetchedCount = Long.valueOf(fetchedCount);
    }
}