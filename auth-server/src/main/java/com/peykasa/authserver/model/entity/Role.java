package com.peykasa.authserver.model.entity;

import com.peykasa.authserver.utility.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@Entity
@Table(name = "tbl_role")
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tbl_role_permission",
            joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "permission_id", referencedColumnName = "id")})
    private Set<Permission> permissions = new HashSet<>();

    public Role(Long id, String name, String description, Set<Permission> permissions) {
        this(id);
        this.name = name;
        this.description = description;
        this.permissions = permissions;
    }

    public Role(Long id) {
        this.id = id;
    }

    @Transient
    public Role copy() {
        Role copy = new Role();
        ObjectMapper.copy(this, copy);
        return copy;
    }
}
