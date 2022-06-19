package com.peykasa.authserver;

import com.peykasa.authserver.model.entity.Client;
import com.peykasa.authserver.model.entity.Permission;
import com.peykasa.authserver.model.entity.Role;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.ClientRepository;
import com.peykasa.authserver.repository.PermissionRepository;
import com.peykasa.authserver.repository.RoleRepository;
import com.peykasa.authserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
public class DatabaseUtil {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("roleRepository")
    private RoleRepository roleRepository;

    @Autowired
    @Qualifier("permissionRepository")
    private PermissionRepository permissionRepository;

    @Autowired
    private ClientRepository clientRepository;

    public void init() {
        init(true);
    }

    public void init(boolean loadPerms) {
        userRepository.deleteAllInBatch();
        roleRepository.deleteAll();
        if (loadPerms) {
            List<Permission> all = permissionRepository.findAll((Specification<Permission>) null);
            all.sort((o1, o2) -> o2.getId().compareTo(o1.getId()));
            for (Permission permission : all) {
                permissionRepository.delete(permission);
            }
        }
        clientRepository.deleteAll();
        if (loadPerms) {
            List<Object[]> perms = new ArrayList<Object[]>() {
                {
                    add(new Object[]{"User Management", "user_management", "User Management", null});
                    add(new Object[]{"Role Management", "role_management", "Role Management", null});
                    add(new Object[]{"Auth Server User Create", "auth_server_user_create", "Auth Server User Create", 0});
                    add(new Object[]{"Auth Server User Read", "auth_server_user_read", "Auth Server User Read", 0});
                    add(new Object[]{"Auth Server User Update", "auth_server_user_update", "Auth Server User Update", 0});
                    add(new Object[]{"Auth Server User Delete", "auth_server_user_delete", "Auth Server User Delete", 0});
                    add(new Object[]{"Auth Server Role Create", "auth_server_role_create", "Auth Server Role Create", 1});
                    add(new Object[]{"Auth Server Role Read", "auth_server_role_read", "Auth Server Role Read", 1});
                    add(new Object[]{"Auth Server Role Update", "auth_server_role_update", "Auth Server Role Update", 1});
                    add(new Object[]{"Auth Server Role Delete", "auth_server_role_delete", "Auth Server Role Delete", 1});
                }
            };
            {
                List<Permission> parents = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    Object[] perm = perms.get(i);
                    Permission permission = new Permission();
                    permission.setDescription((String) perm[0]);
                    permission.setPermission((String) perm[1]);
                    permission.setTitle((String) perm[2]);
                    permission.setParentId((Long) perm[3]);
                    Permission save = permissionRepository.save(permission);
                    parents.add(save);
                }
                for (int i = 2; i < perms.size(); i++) {
                    Object[] perm = perms.get(i);
                    Permission permission = new Permission();
                    permission.setDescription((String) perm[0]);
                    permission.setPermission((String) perm[1]);
                    permission.setTitle((String) perm[2]);
                    permission.setParentId(parents.get((Integer) perm[3]).getId());
                    permissionRepository.save(permission);
                }
            }
        }
        {
            Role role = new Role();
            role.setName("admin");
            role.setDescription("admin");
            role.setId(1L);
            role.setPermissions(new HashSet<>(permissionRepository.findAll((Specification<Permission>) null)));
            roleRepository.saveAndFlush(role);
        }
        {
            Client client = new Client();
            client.setClientId("portal-client-id");
            client.setClientSecret("portal-client-secret");
            client.setGrantType("password");
            client.setAccessTokenValiditySeconds(300);
            client.setRefreshTokenValiditySeconds(6000);
            Client client1 = new Client();
            client1.setClientId("test-client-id");
            client1.setClientSecret("test-client-secret");
            client1.setGrantType("client_credentials");
            client1.setAccessTokenValiditySeconds(3000);
            client1.setRefreshTokenValiditySeconds(6000);
            clientRepository.save(client);
            clientRepository.save(client1);
        }
        {
            User user = new User();
            user.setFirstName("admin");
            user.setLastName("admin");
            user.setClient(clientRepository.findOne("portal-client-id"));
            user.setUsername("admin");
            user.setPassword("1");
            user.setCreationDate(new Date());
            user.setEnabled(true);
            user.setDeleted(false);
            user.setSuperAdmin(true);
            user.setRoles(new HashSet<>(roleRepository.findAll()));
            userRepository.save(user);
        }
    }
}
