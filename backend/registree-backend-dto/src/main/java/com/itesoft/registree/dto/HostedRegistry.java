package com.itesoft.registree.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.validation.annotation.Validated;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, defaultImpl = HostedRegistry.class)
@Validated
public class HostedRegistry extends StorageCapableRegistry {
  @JsonIgnore
  @Override
  public boolean isDoStore() {
    return true;
  }
}
