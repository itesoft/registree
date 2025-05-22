package com.itesoft.registree.dto.mapper;

import java.util.HashMap;
import java.util.Map;

import com.itesoft.registree.dao.jpa.RouteEntity;
import com.itesoft.registree.dto.Route;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = RegistreeMapperConfig.class)
public interface RouteEntityToRouteMapper
    extends Converter<RouteEntity, Route> {
  Map<String, String> PROPERTY_MAPPINGS = new HashMap<>();

  @Override
  @Mapping(expression = "java(com.itesoft.registree.dto.UserIdentifier.builder()"
    + "   .id(routeEntity.getUser().getId())"
    + "   .username(routeEntity.getUser().getUsername())"
    + "   .build())",
           target = "userIdentifier")
  Route convert(RouteEntity routeEntity);
}
