package com.peykasa.audit.rest;

import com.peykasa.audit.common.Constants;
import com.peykasa.audit.domain.model.AuditModel;
import com.peykasa.audit.exception.GlobalException;
import com.peykasa.audit.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;

/**
 * @author Yaser(amin) Sadeghi
 */
@RestController
@RequestMapping(Constants.LOG_BASE)
public class ReceiveController {
    private final AuditService auditService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveController.class);

    @Autowired
    public ReceiveController(AuditService auditService) {
        this.auditService = auditService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public boolean log(@RequestBody AuditModel body) throws GlobalException {
        LOGGER.debug("Enter Receiver");
        LOGGER.info("request body {}", body);
//        if (body == null) {
//            throw new ValidationException(Messages.INPUT_NULL);
//        }
//        LOGGER.debug("Receiving body {}", body.toString());
//        if (StringUtils.isBlank(body.getActor()))
//            throw new ValidationException(Messages.ACTOR_NULL);
//        if (StringUtils.isBlank(body.getContext()))
//            throw new ValidationException(Messages.CONTEXT_NULL);
//        if (StringUtils.isBlank(body.getEvent()))
//            throw new ValidationException(Messages.EVENT_NULL);
//        if (body.getTime() == null)
//            throw new ValidationException(Messages.TIME_NULL);

        if (body == null) {
            body = new AuditModel();
        }
        if (body.getTime() == null) {
            body.setTime(new Date());
        }
        try {
            if (body.getContext() != null)
                body.setContext(body.getContext().toUpperCase());
            if (body.getEvent() != null)
                body.setEvent(body.getEvent().toUpperCase());
            if (body.getStatus() != null)
                body.setStatus(body.getStatus().toUpperCase());
            auditService.log(body);
        } catch (Exception e) {
            LOGGER.error(body.toString(), e);
            throw new GlobalException(Collections.singletonList(e.getMessage()));
        }
        return true;
    }


}
