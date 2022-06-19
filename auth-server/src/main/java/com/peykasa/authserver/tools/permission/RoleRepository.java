package com.peykasa.authserver.tools.permission;

import com.peykasa.authserver.model.entity.Role;
import com.peykasa.authserver.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Taher Khorshidi
 */
@Repository("toolRoleRep")
public interface RoleRepository extends BaseRepository<Role, Long> {

    @Query(value = "select distinct r from Role r join r.permissions p where p.id=?1")
    List<Role> findByPermission(Long permissionId);
}
