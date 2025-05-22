package com.itesoft.registree.dto.mapper;

import com.itesoft.registree.dao.jpa.RegistryEntity;
import com.itesoft.registree.dto.UpdateRegistryArgs;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = RegistreeMapperConfig.class)
public interface UpdateRegistryArgsToRegistryEntityMapper
    extends Converter<UpdateRegistryArgs, RegistryEntity> {
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "format", ignore = true)
  @Mapping(target = "type", ignore = true)
  @Override
  RegistryEntity convert(UpdateRegistryArgs updateRegistryArgs);
}
