package com.itesoft.registree.dto.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring")
@SpringMapperConfig(conversionServiceAdapterClassName = "RegistreeConversionServiceAdapter",
                    conversionServiceAdapterPackage = "com.itesoft.registree.dto.mapper.adapter")
public interface RegistreeMapperConfig {
}
