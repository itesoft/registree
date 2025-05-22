package com.itesoft.registree.dto.mapper;

import com.itesoft.registree.dto.Gav;
import com.itesoft.registree.dto.UpdateComponentArgs;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = RegistreeMapperConfig.class)
public interface UpdateComponentArgsToGavMapper
    extends Converter<UpdateComponentArgs, Gav> {
  @Override
  Gav convert(UpdateComponentArgs updateComponentArgs);
}
