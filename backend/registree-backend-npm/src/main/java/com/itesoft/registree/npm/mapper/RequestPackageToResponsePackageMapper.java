package com.itesoft.registree.npm.mapper;

import com.itesoft.registree.npm.dto.json.RequestPackage;
import com.itesoft.registree.npm.dto.json.ResponsePackage;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = NpmRegistryMapperConfig.class)
public interface RequestPackageToResponsePackageMapper
    extends Converter<RequestPackage, ResponsePackage> {
  @Mapping(target = "time", ignore = true)
  @Override
  ResponsePackage convert(RequestPackage requestPackage);
}
