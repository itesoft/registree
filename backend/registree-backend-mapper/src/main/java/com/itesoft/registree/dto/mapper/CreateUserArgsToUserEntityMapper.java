package com.itesoft.registree.dto.mapper;

import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dto.CreateUserArgs;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import org.mapstruct.Mapping;

@Mapper(config = RegistreeMapperConfig.class)
public interface CreateUserArgsToUserEntityMapper
    extends Converter<CreateUserArgs, UserEntity> {
  @Mapping(target = "id", ignore = true)
  @Override
  UserEntity convert(CreateUserArgs createUserArgs);
}
