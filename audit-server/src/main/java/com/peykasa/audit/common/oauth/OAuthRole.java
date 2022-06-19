package com.peykasa.audit.common.oauth;

import lombok.Data;

import java.util.Set;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
public class OAuthRole {
    private Long id;
    private String name;
    private String description;
    private Set<OAuthPermission> permissions;
    @Override
    public String toString() {
        return "OAuthRole{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}
