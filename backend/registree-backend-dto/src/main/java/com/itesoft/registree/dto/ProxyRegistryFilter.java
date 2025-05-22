package com.itesoft.registree.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

@Validated
public class ProxyRegistryFilter {
  @NotNull
  @NotEmpty
  private String pathPrefix;
  @NotNull
  private ProxyRegistryFilterPolicy policy;

  public String getPathPrefix() {
    return pathPrefix;
  }

  public void setPathPrefix(final String pathPrefix) {
    this.pathPrefix = pathPrefix;
  }

  public ProxyRegistryFilterPolicy getPolicy() {
    return policy;
  }

  public void setPolicy(final ProxyRegistryFilterPolicy policy) {
    this.policy = policy;
  }
}
