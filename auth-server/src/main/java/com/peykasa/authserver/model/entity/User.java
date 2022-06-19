package com.peykasa.authserver.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.peykasa.authserver.utility.ObjectMapper;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;

import javax.persistence.*;
import java.util.*;

import static com.peykasa.authserver.Constants.AUTHORITY_SUPER_ADMIN;
import static com.peykasa.authserver.Constants._DELETED_AT;
import static com.peykasa.authserver.utility.DurationUtil.addDurationToCalendar;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
@Entity
@Table(name = "tbl_user")
@SQLDelete(sql = "update tbl_user set deleted=true,enabled=false, username=CONCAT(username, CONCAT('" + _DELETED_AT + "', CURRENT_TIMESTAMP)) where id = ?")
@Where(clause = "deleted = false")
//@EntityListeners(value = JPAAspect.class)
public class User implements UserDetails, Cloneable {

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
    @Column()
    private Date passExpirationDate;

    @Column()
    private Date lastLoginAttemptTime;

    @Column()
    private Date blockDate;

    @Column()
    private Integer blockCount;

    @Column()
    private Boolean superAdmin = false;

    private boolean deleted = false;
    private Date creationDate;
    private Date modifiedDate;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tbl_user_role",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")})
    private Set<Role> roles = new HashSet<>();


    @JsonIgnore
    @Transient
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //this means user credintial is
    @Transient
    public boolean isAccountExpired() {
        return getPassExpirationDate() != null && new Date().after(getPassExpirationDate());
    }

    @JsonIgnore
    @Transient
    @Override
    public boolean isAccountNonLocked() {
        return getBlockDate() == null || new Date().after(getBlockDate());
    }

    //must always return true . because user wants to get token
    @JsonIgnore
    @Transient
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Transient
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (superAdmin != null && !superAdmin && isAccountExpired())
            return new ArrayList<>();
        ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if (roles == null) {
            return grantedAuthorities;
        }
        for (Role role : roles) {
            if (role.getPermissions() == null)
                continue;
            for (Permission permission : role.getPermissions()) {
                grantedAuthorities.add(new SimpleGrantedAuthority(permission.getPermission()));
            }
        }
        if (superAdmin != null && superAdmin)
            grantedAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_SUPER_ADMIN));
        return grantedAuthorities;
    }

    protected final static Logger LOGGER = LoggerFactory.getLogger(User.class);

    @Transient
    public void setPasswordExpiry(Date start, long millis) {
        LOGGER.info("Setting expiration date {}", millis);
        Calendar calendar = addDurationToCalendar(start, millis);
        setPassExpirationDate(calendar.getTime());
    }

    public boolean hasPermission(String method, String url) {
        if (superAdmin != null && superAdmin) {
            return true;
        }
        if (roles != null)
            for (Role role : roles) {
                for (Permission perm : role.getPermissions()) {
                    if (check(perm.getUrls(), method, url))
                        return true;
                }
            }
        return false;
    }

    private boolean check(Set<PermissionURL> urls, String method, String url) {
        url = url.replaceAll(".*/api/(.*)", "/api/$1");
        for (PermissionURL permissions : urls) {
            LOGGER.info("Checking method '{}' with config {}", method, permissions);
            if (permissions.getMethod().equalsIgnoreCase(method)) {
                AntPathMatcher antPathMatcher = new AntPathMatcher();
                boolean match = antPathMatcher.match(permissions.getUrl(), url);
                LOGGER.info("Checking url '{}' with config {}", url, permissions);
                if (match) {
                    LOGGER.info("URl '{}' and method '{}' matched with config {}", url, method, permissions);
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        User user = new User();
        Set<PermissionURL> s = new HashSet<>();
        s.add(new PermissionURL("get", "/api/users/**/perms", null));
        s.add(new PermissionURL("post", "/api/users", null));
        s.add(new PermissionURL("delete", "/api/users/delete", null));

        System.out.println(user.check(s, "get", "/olap/v1233/api/users/123/a/a/a/a//perms"));

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        User ret = new User();
        ret.setBlockCount(getBlockCount());
        ret.setBlockDate(getBlockDate());
        ret.setClient((Client) client.clone());
        ret.setCreationDate(getCreationDate());
        ret.setDeleted(isDeleted());
        ret.setEnabled(isEnabled());
        ret.setFirstName(getFirstName());
        ret.setId(getId());
        ret.setLastLoginAttemptTime(getLastLoginAttemptTime());
        ret.setLastName(getLastName());
        ret.setModifiedDate(getModifiedDate());
        ret.setPassExpirationDate(getPassExpirationDate());
        ret.setPassword(getPassword());
        for (Role role : getRoles()) {
            ret.getRoles().add(new Role(role.getId(), role.getName(), role.getDescription(), role.getPermissions()));
        }
        ret.setSuperAdmin(getSuperAdmin());
        ret.setUsername(getUsername());
        return ret;
    }

    @Transient
    public User copy() {
        User copy = new User();
        ObjectMapper.copy(this, copy);
        return copy;
    }
}
