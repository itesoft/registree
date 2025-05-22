package com.itesoft.registree.npm.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring")
@SpringMapperConfig(conversionServiceAdapterClassName = "NpmRegistryConversionServiceAdapter",
                    conversionServiceAdapterPackage = "com.itesoft.registree.npm.mapper.adapter")
public interface NpmRegistryMapperConfig {
}
