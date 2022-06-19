package com.peykasa.authserver.repository;

import com.peykasa.authserver.model.entity.Permission;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taher Khorshidi
 */
@RepositoryRestResource
public interface PermissionRepository extends PagingAndSortingRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    @RestResource()
    @Override
    default Page<Permission> findAll(Pageable pageable) {
        if (pageable == null) {
            Iterable<Permission> all = findAll();
            ArrayList<Permission> permissions = new ArrayList<>();
            all.forEach(permissions::add);
            return new PageImpl<>(permissions, new PageRequest(0, permissions.size()<1?1: permissions.size()), permissions.size());
        }
        return findAll(null, pageable);
    }

    @RestResource(exported = false)
    @Override
    Permission findOne(Specification<Permission> specification);

    @RestResource(exported = false)
    @Override
    List<Permission> findAll(Specification<Permission> specification);

    @RestResource(exported = false)
    @Override
    Page<Permission> findAll(Specification<Permission> specification, Pageable pageable);

    @RestResource(exported = false)
    @Override
    List<Permission> findAll(Specification<Permission> specification, Sort sort);

    @RestResource(exported = false)
    @Override
    long count(Specification<Permission> specification);

    @RestResource(exported = false)
    @Override
    Iterable<Permission> findAll(Sort sort);

    @RestResource(exported = false)
    @Override
    <S extends Permission> S save(S entity);

    @RestResource(exported = false)
    @Override
    <S extends Permission> Iterable<S> save(Iterable<S> entities);

    @RestResource(exported = false)
    @Override
    Permission findOne(Long aLong);

    @RestResource(exported = false)
    @Override
    boolean exists(Long aLong);

    @RestResource(exported = false)
    @Override
    Iterable<Permission> findAll();

    @RestResource(exported = false)
    @Override
    Iterable<Permission> findAll(Iterable<Long> longs);

    @RestResource(exported = false)
    @Override
    long count();

    @RestResource(exported = false)
    @Override
    void delete(Long aLong);

    @RestResource(exported = false)
    @Override
    void delete(Permission entity);

    @RestResource(exported = false)
    @Override
    void delete(Iterable<? extends Permission> entities);

    @RestResource(exported = false)
    @Override
    void deleteAll();
}
