package com.peykasa.authserver.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    default Page<T> findAlls(Pageable pageable) {
        if (pageable == null) {
            Iterable<T> all = findAll();
            List<T> users = new ArrayList<>();
            all.forEach(users::add);
            if (!users.isEmpty())
                return new PageImpl<>(users, new PageRequest(0, users.size()), users.size());
            else {
                return new PageImpl<>(new ArrayList<>());
            }
        }
        return null;
    }
}
