package com.peykasa.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.peykasa.audit.domain.model.AuditModel;
import com.peykasa.audit.domain.model.SearchCTO;
import com.peykasa.audit.exception.GlobalException;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
public interface AuditService {
    void log(AuditModel body) throws JsonProcessingException, GlobalException;

    Page<AuditModel> search(SearchCTO searchCTO);

    List<String> distinct(String fieldName);

    List<String> distinctBy(String parent, String fieldName,String parentFilter);

    int getMaxUIResult();

}
