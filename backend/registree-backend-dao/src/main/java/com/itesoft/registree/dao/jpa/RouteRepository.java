package com.itesoft.registree.dao.jpa;

import java.util.List;
import java.util.Optional;

import com.itesoft.registree.dao.RegistreeRepository;

public interface RouteRepository extends RegistreeRepository<RouteEntity, Long> {
  List<RouteEntity> findAllByUserIdAndPermissionsContaining(long userId, String permission);

  Optional<RouteEntity> findByUserIdAndPath(long userId, String path);

  void deleteByUserIdAndPath(long userId, String path);
}
