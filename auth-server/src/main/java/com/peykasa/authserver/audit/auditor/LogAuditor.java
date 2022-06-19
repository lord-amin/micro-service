package com.peykasa.authserver.audit.auditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yaser(amin) Sadeghi
 */
public class LogAuditor implements Auditor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogAuditor.class);

    @Override
    public void audit(Audit audit) {
        LOGGER.info("Sending in thread {} ", Thread.currentThread().getName());
        LOGGER.info("Auditing {}", audit.toString());
    }
}
