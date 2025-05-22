package com.itesoft.registree.dto.mapper;

import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dto.User;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import org.mapstruct.Mapping;

@Mapper(config = RegistreeMapperConfig.class)
public interface UserToUserEntityMapper
    extends Converter<User, UserEntity> {
  @Mapping(target = "password", ignore = true)
  @Override
  UserEntity convert(User user);
}
