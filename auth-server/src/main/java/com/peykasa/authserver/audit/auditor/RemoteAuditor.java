package com.peykasa.authserver.audit.auditor;

import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

/**
 * @author Yaser(amin) Sadeghi
 */
public class RemoteAuditor implements Auditor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteAuditor.class);
    private AuditServiceF auditService;


    public RemoteAuditor(AuditServiceF auditService) {
        this.auditService = auditService;
    }

    @Override
    @Async
    public <S, D> void audit(Audit<S, D> audit) {
        LOGGER.info("Auditing by thread " + Thread.currentThread().getName());
        try {
            var s = audit.toString();
            LOGGER.info("Sending audit {}", s);
            var body = auditService.log(s);
            LOGGER.info("Auditable successfully sent, server-messages:[{}]", body);
        } catch (Exception e) {
            LOGGER.error("Auditable fail ", e);
        }
    }
}
