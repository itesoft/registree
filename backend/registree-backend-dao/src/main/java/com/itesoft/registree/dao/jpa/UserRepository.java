package com.itesoft.registree.dao.jpa;

import java.util.Optional;

import com.itesoft.registree.dao.RegistreeRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends RegistreeRepository<UserEntity, Long> {
  boolean existsByUsername(String username);

  Optional<UserEntity> findByUsername(String username);

  @Query(value = "SELECT * FROM reg_user u WHERE u.username=?1 AND u.password=crypt(?2, u.password)", nativeQuery = true)
  Optional<UserEntity> findByUsernameAndPassword(String username, String password);

  @Modifying
  @Query(value = "UPDATE reg_user SET password=crypt(?2, gen_salt('md5')) WHERE id=?1",
         nativeQuery = true)
  int updatePassword(long id, String password);

  void deleteByUsername(String stringValue);
}
