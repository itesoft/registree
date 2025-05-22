package com.itesoft.registree.dto.mapper;

import java.util.HashMap;
import java.util.Map;

import com.itesoft.registree.dao.jpa.ComponentEntity;
import com.itesoft.registree.dto.Component;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = RegistreeMapperConfig.class)
public interface ComponentEntityToComponentMapper
    extends Converter<ComponentEntity, Component> {
  Map<String, String> PROPERTY_MAPPINGS = new HashMap<>();

  @Override
  @Mapping(expression = "java(componentEntity.getRegistry() == null ? null : componentEntity.getRegistry().getName())",
           target = "registryName")
  @Mapping(expression = "java(componentEntity.getRegistry() == null ? null : componentEntity.getRegistry().getFormat())",
           target = "registryFormat")
  Component convert(ComponentEntity componentEntity);
}
