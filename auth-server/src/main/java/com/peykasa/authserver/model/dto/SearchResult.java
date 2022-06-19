package com.peykasa.authserver.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult<T> {
    private Collection<T> results = new ArrayList<>();
    private long totalElements = 0;
}
