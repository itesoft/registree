package com.itesoft.registree.dto.converter;

import java.util.HashMap;
import java.util.Map;

import com.itesoft.registree.dao.jpa.RegistryEntity;
import com.itesoft.registree.dto.GroupRegistry;
import com.itesoft.registree.dto.HostedRegistry;
import com.itesoft.registree.dto.ProxyRegistry;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.exception.UnprocessableException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RegistryEntityToRegistryConveter
    implements Converter<RegistryEntity, Registry> {
  public static final Map<String, String> PROPERTY_MAPPINGS = new HashMap<>();

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public Registry convert(final RegistryEntity entity) {
    try {
      final Registry registry;
      if (RegistryType.HOSTED.getValue().equals(entity.getType())) {
        registry = objectMapper.readValue(entity.getConfiguration(),
                                          HostedRegistry.class);
      } else if (RegistryType.GROUP.getValue().equals(entity.getType())) {
        registry = objectMapper.readValue(entity.getConfiguration(),
                                          GroupRegistry.class);
      } else if (RegistryType.PROXY.getValue().equals(entity.getType())) {
        registry = objectMapper.readValue(entity.getConfiguration(),
                                          ProxyRegistry.class);
      } else {
        registry = new Registry();
      }
      registry.setName(entity.getName());
      registry.setFormat(entity.getFormat());
      registry.setType(entity.getType());
      registry.setConfiguration(entity.getConfiguration());
      return registry;
    } catch (final JsonProcessingException exception) {
      throw new UnprocessableException("Failed to read registry configuration", exception);
    }
  }
}
