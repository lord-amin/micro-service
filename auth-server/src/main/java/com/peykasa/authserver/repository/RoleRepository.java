package com.peykasa.authserver.repository;

import com.peykasa.authserver.model.entity.Role;
import com.peykasa.authserver.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author Taher Khorshidi
 */
@RepositoryRestResource(exported = false)
public interface RoleRepository extends BaseRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @Override
    default Page<Role> findAll(Pageable pageable) {
        Page<Role> roles = findAlls(pageable);
        if (roles != null)
            return roles;
        return findAll((Specification<Role>) null, pageable);
    }
}
