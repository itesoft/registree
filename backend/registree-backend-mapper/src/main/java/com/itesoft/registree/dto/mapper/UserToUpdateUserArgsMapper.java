package com.itesoft.registree.dto.mapper;

import com.itesoft.registree.dto.UpdateUserArgs;
import com.itesoft.registree.dto.User;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = RegistreeMapperConfig.class)
public interface UserToUpdateUserArgsMapper
    extends Converter<User, UpdateUserArgs> {
  @Override
  UpdateUserArgs convert(User user);
}
