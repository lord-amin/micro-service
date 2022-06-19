package com.peykasa.authserver.repository;

import com.peykasa.authserver.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

/**
 * @author Taher Khorshidi, Yaser(amin) Sadeghi
 */
@RepositoryRestResource(exported = false)
public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findById(Long id);

    @Query(value = "select e.username from tbl_user e where e.id=:id ", nativeQuery = true)
    String getUserById(@Param("id") Long id);

    @Override
    default Page<User> findAll(Pageable pageable) {
        Page<User> users = findAlls(pageable);
        if (users != null)
            return users;
        return findAll((Specification<User>) null, pageable);
    }

    Page<User> findBySuperAdmin(boolean sa, Pageable pageable);

    @Query("select u from User u where u.username=:username")
    User fetchByUsername(@Param("username") String username);

    @Query("select u from User u where u.client.clientId=:clientId and u.superAdmin=true")
    List<User> fetchByClientIdAndIsSuperAdmin(@Param("clientId") String clientId);

    //    @Query("select u from User u where u.client.clientId=:clientId and u.superAdmin=true")
    Page<User> findBySuperAdmin(Pageable pageable, boolean superAdmin);

    Optional<User> findByUsername(String username);

    User findByUsernameAndClient_clientId(String username, String clientId);

    @SuppressWarnings("all")
    @Override
    User findOne(Long aLong);

    @Override
    default <S extends User> S saveAndFlush(S s) {
        System.out.println("ID : " + s.getId());
        S save = save(s);
        flush();
        return save;
    }
}
