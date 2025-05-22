package com.itesoft.registree.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

@Validated
public class ProxyRegistryFiltering {
  @NotEmpty
  private List<ProxyRegistryFilter> filters;
  @NotNull
  private ProxyRegistryFilterPolicy defaultPolicy;

  public List<ProxyRegistryFilter> getFilters() {
    return filters;
  }

  public void setFilters(final List<ProxyRegistryFilter> filters) {
    this.filters = filters;
  }

  public ProxyRegistryFilterPolicy getDefaultPolicy() {
    return defaultPolicy;
  }

  public void setDefaultPolicy(final ProxyRegistryFilterPolicy defaultPolicy) {
    this.defaultPolicy = defaultPolicy;
  }
}
