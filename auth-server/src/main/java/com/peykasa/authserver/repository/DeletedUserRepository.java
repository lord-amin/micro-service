package com.peykasa.authserver.repository;

import com.peykasa.authserver.model.entity.DeletedUser;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author Taher Khorshidi
 */
@RepositoryRestResource(exported = false)
public interface DeletedUserRepository extends PagingAndSortingRepository<DeletedUser, Long> {
}
