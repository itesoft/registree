package com.itesoft.registree.controller;

import jakarta.transaction.Transactional;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.dao.jpa.RouteEntity;
import com.itesoft.registree.dao.jpa.RouteRepository;
import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dao.jpa.UserRepository;
import com.itesoft.registree.dto.CreateRouteArgs;
import com.itesoft.registree.dto.DeleteRouteArgs;
import com.itesoft.registree.dto.OneOfLongOrString;
import com.itesoft.registree.dto.Route;
import com.itesoft.registree.exception.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;

@Transactional(rollbackOn = Throwable.class)
@Controller
public class UserRouteController {
  @Autowired
  private ConversionService conversionService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RouteRepository routeRepository;

  public Route createRoute(final RequestContext requestContext,
                           final ResponseContext responseContext,
                           final OneOfLongOrString userId,
                           final String path,
                           final CreateRouteArgs createRouteArgs) {
    final UserEntity userEntity = getUserEntity(userId);

    final RouteEntity routeEntity = routeRepository.findByUserIdAndPath(userEntity.getId(), path).orElse(new RouteEntity());
    routeEntity.setUser(userEntity);
    routeEntity.setPath(path);
    routeEntity.setPermissions(createRouteArgs.getPermissions());

    final RouteEntity resultEntity = routeRepository.save(routeEntity);
    return conversionService.convert(resultEntity, Route.class);
  }

  public void deleteRoute(final RequestContext requestContext,
                          final ResponseContext responseContext,
                          final OneOfLongOrString userId,
                          final String path,
                          final DeleteRouteArgs deleteRouteArgs) {
    final UserEntity userEntity = getUserEntity(userId);
    routeRepository.deleteByUserIdAndPath(userEntity.getId(), path);
  }

  private UserEntity getUserEntity(final OneOfLongOrString userId) {
    final UserEntity userEntity;
    if (userId.isLong()) {
      userEntity = userRepository.findById(userId.getLongValue()).orElse(null);
    } else {
      userEntity = userRepository.findByUsername(userId.getStringValue()).orElse(null);
    }
    if (userEntity == null) {
      throw new NotFoundException(String.format("User with id [%s] was not found", userId));
    }
    return userEntity;
  }
}
