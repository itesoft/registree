package com.itesoft.registree.dto.mapper;

import com.itesoft.registree.dao.jpa.RegistryEntity;
import com.itesoft.registree.dto.CreateRegistryArgs;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = RegistreeMapperConfig.class)
public interface CreateRegistryArgsToRegistryEntityMapper
    extends Converter<CreateRegistryArgs, RegistryEntity> {
  @Override
  @Mapping(expression = "java(createRegistryArgs.getType() == null ? null : createRegistryArgs.getType().toLowerCase())",
           target = "type")
  RegistryEntity convert(CreateRegistryArgs createRegistryArgs);
}
