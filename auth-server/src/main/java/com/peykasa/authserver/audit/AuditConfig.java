package com.peykasa.authserver.audit;

import com.peykasa.authserver.audit.aspect.AuditServiceAspect;
import com.peykasa.authserver.audit.auditor.AuditServiceF;
import com.peykasa.authserver.audit.auditor.Auditor;
import com.peykasa.authserver.audit.auditor.RemoteAuditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Yaser(amin) Sadeghi
 */
@Configuration
@EnableAsync
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AuditConfig
        implements AsyncConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditConfig.class);
    @Value("${app.audit.pool.size:1}")
    private Integer poolSize = 2;


    @Bean
    public AuditServiceAspect auditService(Auditor auditor, @Value("${app.audit.admin.enabled:true}") boolean adminLogEnabled) {
        return new AuditServiceAspect(auditor, adminLogEnabled);
    }

    @Bean
    public Auditor auditor(AuditServiceF auditServiceF) {
        return new RemoteAuditor(auditServiceF);
    }

    @Override
    public Executor getAsyncExecutor() {
        return Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(
                Throwable throwable, Method method, Object... obj) {
            LOGGER.error("Exception message '{}' : Method name '{}' : params {}", throwable.getMessage(),
                    method.getName(), obj);
        }
    }
}
