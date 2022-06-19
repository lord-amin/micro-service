package com.peykasa.authserver.model.entity;

import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@Entity
@Table(name = "tbl_user")
@Where(clause = "deleted = true")
public class DeletedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    private boolean deleted = false;
    private Date creationDate;
    private Date modifiedDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tbl_user_role",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")})
    private Set<Role> roles;

}
