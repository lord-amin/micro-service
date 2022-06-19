package com.peykasa.authserver.audit.auditor;

import org.springframework.cloud.netflix.feign.FeignAutoConfiguration;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Kamran Ghiasvand
 */
@FeignClient("audit-service")
@Import(FeignAutoConfiguration.class)
public interface AuditServiceF {
    @PostMapping(path = "/api/action/logs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    String log(String body);
}
