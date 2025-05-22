package com.itesoft.registree.oci.config;

import com.itesoft.registree.dto.ProxyRegistry;

import org.springframework.validation.annotation.Validated;

@Validated
public class OciProxyRegistry extends ProxyRegistry {
  private String proxyUsername;
  private String proxyPassword;

  public String getProxyUsername() {
    return proxyUsername;
  }

  public void setProxyUsername(final String proxyUsername) {
    this.proxyUsername = proxyUsername;
  }

  public String getProxyPassword() {
    return proxyPassword;
  }

  public void setProxyPassword(final String proxyPassword) {
    this.proxyPassword = proxyPassword;
  }
}
