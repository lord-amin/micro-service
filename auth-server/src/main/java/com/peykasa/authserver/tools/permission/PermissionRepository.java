package com.peykasa.authserver.tools.permission;

import com.peykasa.authserver.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Taher Khorshidi
 */
@Repository("toolPermissionRep")
public interface PermissionRepository extends PagingAndSortingRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    List<Permission> findByPermissionAndServiceName(String perm,String sName);
    List<Permission> findByPermission(String perm);
}
