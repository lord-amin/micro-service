package com.peykasa.audit.common.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Yaser(amin) Sadeghi
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OAuthClient {
    private String clientId;
    private String clientSecret;
    private String grantType;
    private Integer accessTokenValiditySeconds = 10 * 60;
    private Integer refreshTokenValiditySeconds = 24 * 60 * 60;
    private Set<String> scope;
    private Set<String> registeredRedirectUri;
    private Set<String> resourceIds;
    private Set<String> authorizedGrantTypes;
    private boolean secretRequired;
    private boolean scoped;
    private Collection<OAuthSimpleGrantedAuthority> authorities;
    private boolean autoApprove;
    private Map<String, Object> additionalInformation;
}
