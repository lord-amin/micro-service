package com.peykasa.authserver.model.entity;

import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

import javax.persistence.*;
import java.util.*;

/**
 * @author Yaser(amin) Sadeghi
 */
@Setter
@Entity
@Table(name = "tbl_client")
public class Client implements ClientDetails, Cloneable {

    private String clientId;
    private String clientSecret;
    private String grantType;
    private Integer accessTokenValiditySeconds = 150 * 60;
    private Integer refreshTokenValiditySeconds = 24 * 60 * 60;

    @Override
    @Id
    @Column(nullable = false)
    public String getClientId() {
        return clientId;
    }

    @Override
    @Column(nullable = false)
    public String getClientSecret() {
        return clientSecret;
    }

    @Column(nullable = false)
    public String getGrantType() {
        return grantType;
    }

    @Override
    @Column(name = "access_token_validity_seconds")
    public Integer getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    @Override
    @Column(name = "refresh_token_validity_seconds")
    public Integer getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }

    @Override
    @Transient
    public Set<String> getResourceIds() {
        return Collections.singleton("oauth2-resource");
    }

    @Override
    @Transient
    public boolean isSecretRequired() {
        return true;
    }

    @Override
    @Transient
    public boolean isScoped() {
        return true;
    }

    @Override
    @Transient
    public Set<String> getScope() {
        return Collections.singleton("read");
    }

    @Override
    @Transient
    public Set<String> getAuthorizedGrantTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(grantType);
        set.add("refresh_token");
        return set;
    }

    @Override
    @Transient
    public Set<String> getRegisteredRedirectUri() {
        return Collections.singleton("");
    }

    @Override
    @Transient
    public Collection<GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("CLIENT_CREDENTIALS"));
    }

    @Override
    @Transient
    public boolean isAutoApprove(String scope) {
        return true;
    }

    @Override
    @Transient
    public Map<String, Object> getAdditionalInformation() {
        return null;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Client client = (Client) super.clone();
        client.setClientId(this.clientId);
        client.setClientSecret(this.clientSecret);
        client.setGrantType(this.grantType);
        client.setRefreshTokenValiditySeconds(this.refreshTokenValiditySeconds);
        return client;
    }
}
