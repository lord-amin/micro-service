package com.peykasa.audit.domain.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author kamran
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AuditModel {
    public static final String CONTEXT_FIELD = "context";
    public static final String EVENT_FIELD = "event";
    public static final String ACTOR_FIELD = "actor";
    public static final String CLIENT_FIELD = "client";
    public static final String STATUS_FIELD = "status";
    public static final String PRIMARY_FIELD = "primary";
    public static final String REMOTE_ADDRESS_FIELD = "remoteAddress";
    public static final String EXTRA_INFO_FIELD = "extraInfo";
    public static final String TIME_FIELD = "time";
    public static final String FROM_STATE_FIELD = "fromState";
    public static final String TO_STATE_FIELD = "toState";
    private String event;
    private String context;
    private String actor;
    private String client;
    private String status;
    private Boolean primary;
    private String remoteAddress;
    private Date time = Date.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
    private Object fromState;
    private Object toState;
    private String extraInfo;
    @JsonIgnore
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String toString() {
        try {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE));
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Audit{" +
                    "event='" + event + '\'' +
                    ", context='" + context + '\'' +
                    ", actor='" + actor + '\'' +
                    ", time=" + time +
                    ", remoteAddress='" + remoteAddress + '\'' +
                    ", fromState=" + fromState +
                    ", toState=" + toState +
                    ", extraInfo=" + extraInfo +
                    '}';
        }
    }

}
