package com.itesoft.registree.dto.mapper;

import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dto.UpdateUserArgs;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import org.mapstruct.Mapping;

@Mapper(config = RegistreeMapperConfig.class)
public interface UpdateUserArgsToUserEntityMapper
    extends Converter<UpdateUserArgs, UserEntity> {
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Override
  UserEntity convert(UpdateUserArgs updateUserArgs);
}
