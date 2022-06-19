package com.peykasa.authserver.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Yaser(amin) Sadeghi
 */
//@Data
@Entity
@Table(name = "tbl_permission")
@NoArgsConstructor
public class Permission {
    public Permission(String d) {
        String[] split = d.split("/");
        id = Long.valueOf(split[split.length - 1]);
    }

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String permission;
    @Getter
    @Setter
    private Long parentId;
    @Getter
    @Setter
    private String serviceName;
    @Getter
    @Setter
    @RestResource(exported = false)
    @OrderBy("id ASC")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "parentId")
    private Set<Permission> children = new HashSet<>();
    @Getter
    @Setter
    @RestResource(exported = false)
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true,mappedBy = "permission")
    private Set<PermissionURL> urls= new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        if (id != null && that.id != null && id.equals(that.id))
            return true;
        if (permission != null && that.permission != null && permission.equals(that.permission))
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (permission != null ? permission.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        return result;
    }
}
