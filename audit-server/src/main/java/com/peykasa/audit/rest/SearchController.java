package com.peykasa.audit.rest;

import com.peykasa.audit.commom.Messages;
import com.peykasa.audit.common.Constants;
import com.peykasa.audit.domain.model.AuditModel;
import com.peykasa.audit.domain.model.SearchCTO;
import com.peykasa.audit.exception.GlobalException;
import com.peykasa.audit.exception.ValidationException;
import com.peykasa.audit.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kamran
 */
@RestController
@RequestMapping(Constants.SEARCH_BASE)
//@Api(description = "Search service ", tags = {"search-service"})
public class SearchController {

    private AuditService service;

    @Autowired
    public SearchController(AuditService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<AuditModel> search(@RequestBody SearchCTO cto) throws GlobalException {
        if (cto == null) {
            cto = new SearchCTO();
        }
        if (cto.getStart() == null)
            throw new ValidationException(Messages.START_NULL);
        if (cto.getEnd() == null)
            throw new ValidationException(Messages.END_NULL);
        if (cto.getStart().compareTo(cto.getEnd()) >= 0)
            throw new ValidationException(Messages.START_BIGGER);
        if (cto.getPage() < 0)
            cto.setPage(0);
        if (cto.getSize() <= 0)
            cto.setSize(10);
        int maxUIResult1 = service.getMaxUIResult();
        int count = cto.getPage() * cto.getSize() + cto.getSize();
        if (count > maxUIResult1)
            throw new ValidationException(String.format(Messages.MAX_UI, maxUIResult1, count));
        return service.search(cto);
    }

    //    @ApiOperation(value = "Get list of events", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = Constants.EVENT_URL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> events() throws GlobalException {
        return service.distinct("event");
    }

    //    @ApiOperation(value = "Get list of events by context", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = Constants.EVENT_BY_CONTEXT_URL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> eventsByContext(String context) throws GlobalException {
        if (context == null || "".equals(context))
            return service.distinct("event");
        return service.distinctBy("context", "event", context);
    }

    //    @ApiOperation(value = "Get list of contexts", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = Constants.CONTEXT_URL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> contexts() throws GlobalException {
        return service.distinct("context");
    }

    //    @ApiOperation(value = "Get list of users", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = Constants.USER_URL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> actors() throws GlobalException {
        return service.distinct("actor");
    }
}
