package com.itesoft.registree.persistence;

import static com.itesoft.registree.persistence.WellKnownRoutePaths.API_V1_TOKENS_ROUTE_PATH;
import static com.itesoft.registree.persistence.WellKnownRoutePaths.ROOT_ROUTE_PATH;
import static com.itesoft.registree.persistence.WellKnownUsers.ADMIN_USERNAME;
import static com.itesoft.registree.persistence.WellKnownUsers.ANONYMOUS_USERNAME;

import java.util.Optional;

import jakarta.annotation.PostConstruct;

import com.itesoft.registree.dao.jpa.RouteEntity;
import com.itesoft.registree.dao.jpa.RouteRepository;
import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dao.jpa.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn("defaultUsersManager")
public class DefaultRoutesManager {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RouteRepository routeRepository;

  @PostConstruct
  public void loadRoutesToDatabase() {
    loadAnonymousRouteToDatabase();
    loadAdminRouteToDatabase();
  }

  private void loadAnonymousRouteToDatabase() {
    final UserEntity userEntity = userRepository.findByUsername(ANONYMOUS_USERNAME).orElseThrow();

    final Optional<RouteEntity> existingRoute = routeRepository.findByUserIdAndPath(userEntity.getId(), API_V1_TOKENS_ROUTE_PATH);
    final RouteEntity routeEntity;
    if (existingRoute.isEmpty()) {
      routeEntity = new RouteEntity();
      routeEntity.setUser(userEntity);
      routeEntity.setPath(API_V1_TOKENS_ROUTE_PATH);
    } else {
      routeEntity = existingRoute.get();
    }

    routeEntity.setPermissions("w");
    routeRepository.save(routeEntity);
  }

  private void loadAdminRouteToDatabase() {
    final UserEntity userEntity = userRepository.findByUsername(ADMIN_USERNAME).orElseThrow();

    final Optional<RouteEntity> existingRoute = routeRepository.findByUserIdAndPath(userEntity.getId(), ROOT_ROUTE_PATH);
    final RouteEntity routeEntity;
    if (existingRoute.isEmpty()) {
      routeEntity = new RouteEntity();
      routeEntity.setUser(userEntity);
      routeEntity.setPath(ROOT_ROUTE_PATH);
    } else {
      routeEntity = existingRoute.get();
    }

    routeEntity.setPermissions("rwd");
    routeRepository.save(routeEntity);
  }
}
