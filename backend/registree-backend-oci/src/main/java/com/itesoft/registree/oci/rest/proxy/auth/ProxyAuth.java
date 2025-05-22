package com.itesoft.registree.oci.rest.proxy.auth;

import java.util.Date;

class ProxyAuth {
  private String token;
  private String accessToken;
  private Integer expiresIn;
  private Date issuedAt;

  public String getToken() {
    return token;
  }

  public void setToken(final String token) {
    this.token = token;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(final String accessToken) {
    this.accessToken = accessToken;
  }

  public Integer getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(final int expiresIn) {
    this.expiresIn = expiresIn;
  }

  public Date getIssuedAt() {
    return issuedAt;
  }

  public void setIssuedAt(final Date issuedAt) {
    this.issuedAt = issuedAt;
  }
}
