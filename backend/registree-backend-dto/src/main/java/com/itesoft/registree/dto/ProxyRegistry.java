package com.itesoft.registree.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.validation.annotation.Validated;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, defaultImpl = ProxyRegistry.class)
@Validated
public class ProxyRegistry extends StorageCapableRegistry {
  private boolean doStore;
  @NotNull
  private String proxyUrl;
  @Min(0)
  private int cacheTimeout;
  @Nullable
  @Valid
  private ProxyRegistryFiltering filtering;

  @Override
  public boolean isDoStore() {
    return doStore;
  }

  public void setDoStore(final boolean doStore) {
    this.doStore = doStore;
  }

  public String getProxyUrl() {
    return proxyUrl;
  }

  public void setProxyUrl(final String proxyUrl) {
    this.proxyUrl = proxyUrl;
  }

  public int getCacheTimeout() {
    return cacheTimeout;
  }

  public void setCacheTimeout(final int cacheTimeout) {
    this.cacheTimeout = cacheTimeout;
  }

  public ProxyRegistryFiltering getFiltering() {
    return filtering;
  }

  public void setFiltering(final ProxyRegistryFiltering filtering) {
    this.filtering = filtering;
  }
}
