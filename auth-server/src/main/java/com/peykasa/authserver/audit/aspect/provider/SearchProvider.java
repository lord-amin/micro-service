package com.peykasa.authserver.audit.aspect.provider;

import com.peykasa.authserver.audit.aspect.provider.dto.SearchInputDTO;
import com.peykasa.authserver.audit.aspect.provider.dto.SearchOutputDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import lombok.var;
import org.springframework.context.annotation.Scope;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Component;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class SearchProvider extends AuditProvider {
    private SearchInputDTO input;
    private SearchOutputDTO searchOutputDTO;

    @SuppressWarnings("all")
    public SearchProvider(Object returning, Object[] params) {
        super(returning, params);
        input = new SearchInputDTO(
                params[0] == null ? null : (String) params[0],
                params[1] == null ? null : (String) params[1],
                params[2] == null ? null : (String) params[2]
        );
        var paged = (PagedResources<Resource<?>>) returning;
        searchOutputDTO = new SearchOutputDTO(paged.getMetadata().getTotalElements(), paged.getContent().size());
    }

    @Override
    public Audit<SearchInputDTO, SearchOutputDTO> provide() {
        return new Audit<>(input, searchOutputDTO);
    }
}
