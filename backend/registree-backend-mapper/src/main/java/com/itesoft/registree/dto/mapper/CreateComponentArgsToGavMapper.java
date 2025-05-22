package com.itesoft.registree.dto.mapper;

import com.itesoft.registree.dto.CreateComponentArgs;
import com.itesoft.registree.dto.Gav;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = RegistreeMapperConfig.class)
public interface CreateComponentArgsToGavMapper
    extends Converter<CreateComponentArgs, Gav> {
  @Override
  Gav convert(CreateComponentArgs createComponentArgs);
}
