package com.itesoft.registree.dto.mapper;

import java.util.HashMap;
import java.util.Map;

import com.itesoft.registree.dao.jpa.FileEntity;
import com.itesoft.registree.dto.File;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = RegistreeMapperConfig.class)
public interface FileEntityToFileMapper
    extends Converter<FileEntity, File> {
  Map<String, String> PROPERTY_MAPPINGS = new HashMap<>();

  @Override
  @Mapping(expression = "java(fileEntity.getRegistry() == null ? null : fileEntity.getRegistry().getName())",
           target = "registryName")
  @Mapping(expression = "java(fileEntity.getComponent() == null ? null : fileEntity.getComponent().getId())",
           target = "componentId")
  File convert(FileEntity fileEntity);
}
