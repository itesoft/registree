package com.itesoft.registree.dto.mapper;

import com.itesoft.registree.dao.jpa.FileEntity;
import com.itesoft.registree.dto.CreateFileArgs;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = RegistreeMapperConfig.class)
public interface CreateFileArgsToFileEntityMapper
    extends Converter<CreateFileArgs, FileEntity> {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "registry", ignore = true)
  @Mapping(target = "component", ignore = true)
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "uploader", ignore = true)
  @Override
  FileEntity convert(CreateFileArgs createFileArgs);
}
