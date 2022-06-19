package com.peykasa.audit.common.oauth;

import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Yaser(amin) Sadeghi
 */
@NoArgsConstructor
public final class OAuthSimpleGrantedAuthority implements GrantedAuthority {
    private String role;

    public void setAuthority(String role) {
        this.role = role;
    }

    public String getAuthority() {
        return this.role;
    }

}

