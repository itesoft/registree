package com.itesoft.registree.dto.mapper;

import com.itesoft.registree.dao.jpa.ComponentEntity;
import com.itesoft.registree.dto.UpdateComponentArgs;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import org.mapstruct.Mapping;

@Mapper(config = RegistreeMapperConfig.class)
public interface UpdateComponentArgsToComponentEntityMapper
    extends Converter<UpdateComponentArgs, ComponentEntity> {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "registry", ignore = true)
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Override
  ComponentEntity convert(UpdateComponentArgs updateComponentArgs);
}
