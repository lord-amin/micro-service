package com.peykasa.authserver.controller;

import com.peykasa.authserver.exception.ResourceNotFoundException;
import com.peykasa.authserver.model.cto.BaseCTO;
import com.peykasa.authserver.model.dto.BaseDTO;
import com.peykasa.authserver.repository.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.peykasa.authserver.utility.ObjectMapper.map;
import static com.peykasa.authserver.utility.ObjectMapper.to;

/**
 * @author Yaser(amin) Sadeghi
 */
@SuppressWarnings("unused")
public abstract class EntityController<DTO extends BaseDTO, CTO extends BaseCTO, T, ID extends Serializable> {
    protected final static Logger LOGGER = LoggerFactory.getLogger(EntityController.class);

    public abstract BaseRepository<T, ID> getRepository();

    @SuppressWarnings("all")
    @Autowired
    protected PagedResourcesAssembler<DTO> pagedResourcesAssembler;

    public PagedResources<Resource<DTO>> findAll(String page, String size, String sort) {
        Pageable pageable = null;
        if (page != null && size != null && !"".equals(page) && !"".equals(size)) {
            pageable = new PageRequest(Integer.valueOf(page), Integer.valueOf(size), parseParameterIntoSort(new String[]{sort}));
        }
        Page<T> all = findAll(pageable);
        List<DTO> dtos = convert(all);
        if (pageable == null) {
            pageable = new PageRequest(0, all.getTotalElements() == 0 ? 1 : (int) all.getTotalElements());
        }
        PageImpl<DTO> totals = new PageImpl<>(dtos, pageable, all.getTotalElements());
        return pagedResourcesAssembler.toResource(totals);
    }

    protected List<DTO> convert(Page<T> all) {
        return to(all.getContent(), support());
    }

    protected abstract Class<DTO> support();

    protected Page<T> findAll(Pageable pageable) {
        return getRepository().findAll(pageable);
    }

    public DTO findOne(@PathVariable("id") ID id) throws ResourceNotFoundException {
        T one = getRepository().findOne(id);
        if (one == null)
            throw new ResourceNotFoundException(null, String.format(notFoundMessage(), id));
        return map(one, support());
    }

    public abstract String notFoundMessage();

    ResponseEntity<?> delete(ID id) throws Exception {
        if (id == null)
            throw new ResourceNotFoundException(null, "Empty id " + id);
        getRepository().delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    protected Sort parseParameterIntoSort(String[] source) {
        List<Sort.Order> allOrders = new ArrayList<>();
        for (String part : source) {
            if (part == null) {
                continue;
            }
            String[] elements = part.split(",");
            Sort.Direction direction = elements.length == 0 ? null : Sort.Direction.fromStringOrNull(elements[elements.length - 1]);
            for (int i = 0; i < elements.length; i++) {
                if (i == elements.length - 1 && direction != null) {
                    continue;
                }
                String property = elements[i];
                if (!StringUtils.hasText(property)) {
                    continue;
                }
                allOrders.add(new Sort.Order(direction, property));
            }
        }
        return allOrders.isEmpty() ? null : new Sort(allOrders);
    }
}
