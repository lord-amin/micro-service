package com.peykasa.authserver.audit.aspect;

import com.peykasa.authserver.Constants;
import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.audit.aspect.provider.EmptyExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.ExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.login.LoginExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.login.LoginProvider;
import com.peykasa.authserver.audit.aspect.provider.login.LogoutExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.login.LogoutProvider;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.audit.auditor.Auditor;
import lombok.var;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Enumeration;

import static com.peykasa.authserver.audit.aspect.AuditUtil.isSuperAdmin;

/**
 * @author Yaser(amin) Sadeghi
 */

@Aspect()
public class AuditServiceAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditServiceAspect.class);
    private Auditor auditor;
    @Autowired
    private HttpServletRequest httpServletRequest;
    private boolean adminLogEnabled;
    @Autowired
    private ApplicationContext applicationContext;
    @Value("${audit.event.key:event}")
    private String eventKey;
    @Value("${audit.context.key:context}")
    private String contextKey;

    @Value("${audit.primary.key:primary}")
    private String primaryKey;

    public AuditServiceAspect(Auditor auditor, boolean adminLogEnabled) {
        this.auditor = auditor;
        this.adminLogEnabled = adminLogEnabled;
    }

    @AfterReturning(value = "@annotation(auditable)", returning = "ret")
    public void audit(JoinPoint jp, Auditable auditable, Object ret) {
        LOGGER.info("Running aspect ");
        if (!adminLogEnabled && isSuperAdmin()) {
            LOGGER.warn("Admin audit disabled");
            return;
        }
        var stateClass = auditable.provider();
        try {
            var before = httpServletRequest.getAttribute("before");
            var args = jp.getArgs();
            if (before != null) {
                args[0] = before;
            }
            AuditProvider state = applicationContext.getBean(stateClass, ret, args);
            var audit = state.provide();
            if (nullAudit(audit))
                return;
            if (audit.getActor() == null || "".equals(audit.getActor()))
                audit.setActor(AuditUtil.getLoggedInUser());
            audit.setStatus(Constants.SUCCESS);
            setProperties(auditable, audit);
            overrideProperties(audit, stateClass);
            auditor.audit(audit);
        } catch (Exception e) {
            LOGGER.error("audit error ", e);
        }
    }

    @AfterThrowing(value = "@annotation(auditable)", throwing = "t")
    public void audit(JoinPoint jp, Throwable t, Auditable auditable) {
        LOGGER.info("Running aspect EX");
        if (!adminLogEnabled && isSuperAdmin()) {
            LOGGER.warn("Admin audit disabled");
            return;
        }
        var stateClass = auditable.exProvider();
        if (stateClass.getSimpleName().equals(EmptyExceptionProvider.class.getSimpleName())) {
            LOGGER.info("No auditing EX provided");
            return;
        }
        try {
            ExceptionProvider state = applicationContext.getBean(stateClass, jp.getArgs(), t);
            Audit<?, AbstractMap.SimpleEntry<String, String>> audit = state.provide();
            if (nullAudit(audit)) {
                return;
            }
            if (audit.getActor() == null || "".equals(audit.getActor()))
                audit.setActor(AuditUtil.getLoggedInUser());
            if (audit.getStatus() == null)
                audit.setStatus(Constants.FAIL);
            setProperties(auditable, audit);
            overrideProperties(audit, stateClass);
            auditor.audit(audit);
        } catch (Exception e) {
            LOGGER.error("audit error ", e);
        }
        LOGGER.info("Running audit after exception");
    }

    private void setProperties(Auditable auditable, Audit<?, ?> audit) {
        if (isNull(auditable.context(), "context")) {
            return;
        }
        audit.setContext(auditable.context());
        if (isNull(auditable.event(), "event")) {
            return;
        }
        audit.setEvent(auditable.event());
        audit.setTime(new Date());
        audit.setRemoteAddress(httpServletRequest.getRemoteAddr());

    }

    private void overrideProperties(Audit actionLog, Class stateClass) {
        var event = httpServletRequest.getHeader(eventKey);
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()){
            String o = headerNames.nextElement();
            LOGGER.info("{}=>{}", o,httpServletRequest.getHeader(o));
        }
        LOGGER.info("Finding header '{}' , value is '{}'", eventKey, event);
        if (event != null && !"".equals(event))
            actionLog.setEvent(event);
        var context = httpServletRequest.getHeader(contextKey);
        LOGGER.info("Finding header '{}' , value is '{}'", contextKey, context);
        if (context != null && !"".equals(context))
            actionLog.setContext(context);
        var primary = httpServletRequest.getHeader(primaryKey);
        LOGGER.info("Finding header '{}' , value is '{}'", primaryKey, primary);
        if (primary != null && !"".equals(primary)) {
            if (primary.equalsIgnoreCase("true"))
                actionLog.setPrimary(true);
            if (primary.equalsIgnoreCase("yes"))
                actionLog.setPrimary(true);
            if (primary.equalsIgnoreCase("1"))
                actionLog.setPrimary(true);
        }
        if (stateClass.equals(LoginExceptionProvider.class) ||
                stateClass.equals(LoginProvider.class) ||
                stateClass.equals(LogoutExceptionProvider.class) || stateClass.equals(LogoutProvider.class)) {
            actionLog.setPrimary(true);
        }
    }

    private boolean nullAudit(Audit<?, ?> audit) {
        if (audit == null) {
            LOGGER.error("The audit is null , so ignore it ");
            return true;
        }
        return false;
    }

    private boolean isNull(String context, String log) {
        if ("".equals(context)) {
            LOGGER.error("The {} is empty, so ignore it ", log);
            return true;
        }
        return false;
    }
}