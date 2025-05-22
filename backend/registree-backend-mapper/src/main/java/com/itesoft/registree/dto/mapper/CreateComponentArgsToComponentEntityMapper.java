package com.itesoft.registree.dto.mapper;

import com.itesoft.registree.dao.jpa.ComponentEntity;
import com.itesoft.registree.dto.CreateComponentArgs;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import org.mapstruct.Mapping;

@Mapper(config = RegistreeMapperConfig.class)
public interface CreateComponentArgsToComponentEntityMapper
    extends Converter<CreateComponentArgs, ComponentEntity> {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "registry", ignore = true)
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Override
  ComponentEntity convert(CreateComponentArgs createComponentArgs);
}
