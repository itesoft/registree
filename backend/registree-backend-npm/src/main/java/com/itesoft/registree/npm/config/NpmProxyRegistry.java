package com.itesoft.registree.npm.config;

import com.itesoft.registree.dto.ProxyRegistry;

import org.springframework.validation.annotation.Validated;

@Validated
public class NpmProxyRegistry extends ProxyRegistry {
  private String proxyAuthToken;

  public String getProxyAuthToken() {
    return proxyAuthToken;
  }

  public void setProxyAuthToken(final String proxyAuthToken) {
    this.proxyAuthToken = proxyAuthToken;
  }
}
