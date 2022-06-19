package com.peykasa.audit.common.oauth;

import lombok.Data;

import java.util.Set;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
public class OAuthPermission {
    private Long id;
    private String description;
    private String title;
    private String permission;
    private Long parentId;
    private Set<OAuthPermission> children;
    @Override
    public String toString() {
        return "OAuthPermission{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", title='" + title + '\'' +
                ", action='" + permission + '\'' +
                ", parentId=" + parentId +
                ", children=" + children +
                '}';
    }
}

