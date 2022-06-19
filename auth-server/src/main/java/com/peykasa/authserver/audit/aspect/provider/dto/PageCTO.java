package com.peykasa.authserver.audit.aspect.provider.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@NoArgsConstructor
public class PageCTO {
    private Integer start;
    private Integer size;

    public PageCTO(Integer start, Integer size) {
        this.start = start;
        this.size = size;
    }
}
