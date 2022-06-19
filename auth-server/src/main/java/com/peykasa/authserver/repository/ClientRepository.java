package com.peykasa.authserver.repository;

import com.peykasa.authserver.model.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author Taher Khorshidi
 */
@RepositoryRestResource(exported = false)
public interface ClientRepository extends BaseRepository<Client, String> {

    Client findByClientId(@Param("clientId") String clientId);

    @Override
    default Page<Client> findAll(Pageable pageable) {
        Page<Client> roles = findAlls(pageable);
        if (roles != null)
            return roles;
        return findAll((Specification<Client>) null, pageable);
    }

}
