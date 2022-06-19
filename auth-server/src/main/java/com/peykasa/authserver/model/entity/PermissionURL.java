package com.peykasa.authserver.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * @author Yaser(amin) Sadeghi
 */
//@Data
@Entity
@Table(name = "tbl_permission_url")
@NoArgsConstructor
public class PermissionURL {
    public PermissionURL(String method, String url, Permission permission) {
        this.method = method;
        this.url = url;
        this.permission = permission;
    }

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Getter
    @Setter
    private String method;
    @Getter
    @Setter
    private String url;
    @Getter
    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PermissionURL that = (PermissionURL) o;

        if (id != null && that.id != null && id.equals(that.id))            return true;

        if (permission != null && that.permission != null && permission.getId() != null && that.permission.getId() != null && permission.getId().equals(that.permission.getId()) && url != null
                && that.url != null && url.equals(that.url) && method != null && that.method != null && method.equals(that.method))
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (permission != null && permission.getId() != null ? permission.getId().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PermissionURL{" +
                "id=" + id +
                ", method='" + method + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
