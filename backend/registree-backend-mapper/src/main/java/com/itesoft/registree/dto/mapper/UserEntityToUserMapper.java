package com.itesoft.registree.dto.mapper;

import java.util.HashMap;
import java.util.Map;

import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dto.User;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = RegistreeMapperConfig.class)
public interface UserEntityToUserMapper
    extends Converter<UserEntity, User> {
  Map<String, String> PROPERTY_MAPPINGS = new HashMap<>();

  @Override
  User convert(UserEntity userEntity);
}
