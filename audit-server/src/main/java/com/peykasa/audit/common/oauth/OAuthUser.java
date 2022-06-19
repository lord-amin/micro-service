package com.peykasa.audit.common.oauth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthUser implements UserDetails {

    private long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean enabled = true;
    private boolean deleted = false;
    private Date creationDate;
    private Date modifiedDate;
    private OAuthClient client;
    private Set<OAuthRole> roles;
    private boolean superAdmin = false;

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (OAuthRole role : roles) {
            if (role.getPermissions() == null)
                continue;
            for (OAuthPermission permission : role.getPermissions()) {
                grantedAuthorities.add(new SimpleGrantedAuthority(permission.getPermission()));
            }
        }
        return grantedAuthorities;
    }

    @Override
    public String toString() {
        return "OAuthUser{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", enabled=" + enabled +
                ", deleted=" + deleted +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                ", superAdmin=" + superAdmin +
                '}';
    }

    public boolean hasPermission(List<String> permissions) {
        if(isSuperAdmin()){
            return  true;
        }
        if (permissions == null || permissions.isEmpty())
            return false;
        for (OAuthRole role : roles) {
            for (OAuthPermission perm : role.getPermissions()) {
                if (check(permissions, perm))
                    return true;
            }
        }
        return false;
    }

    private boolean check(List<String> permissions, OAuthPermission perm) {
        for (String item : permissions) {
            if (perm.getPermission() != null && perm.getPermission().equals(item))
                return true;
        }
        return false;
    }

}
