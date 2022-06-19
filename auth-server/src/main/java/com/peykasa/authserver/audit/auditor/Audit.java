package com.peykasa.authserver.audit.auditor;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
public class Audit<S, D> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Audit.class);
    private String event;
    private String context;
    private String actor;
    private String status;
    private String extraInfo;
    private Date time;
    private String remoteAddress;
    private S fromState;
    private D toState;
    private boolean primary = false;
    @JsonIgnore
    private ObjectMapper mapper = new ObjectMapper();

    public Audit(S fromState, D toState) {
        this.fromState = fromState;
        this.toState = toState;
    }

    @Override
    public String toString() {
        try {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.setVisibility(mapper.getSerializationConfig()
                    .getDefaultVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE));
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOGGER.error("", e);
            return "Audit{" +
                    "event='" + event + '\'' +
                    ", context='" + context + '\'' +
                    ", actor='" + actor + '\'' +
                    ", status='" + status + '\'' +
                    ", extraInfo='" + extraInfo + '\'' +
                    ", time=" + time +
                    ", remoteAddress='" + remoteAddress + '\'' +
                    ", fromState=" + fromState +
                    ", toState=" + toState +
                    '}';
        }
    }
}
