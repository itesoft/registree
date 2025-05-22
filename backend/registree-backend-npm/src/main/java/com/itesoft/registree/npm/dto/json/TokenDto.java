package com.itesoft.registree.npm.dto.json;

public class TokenDto extends OkDto {
  private String token;

  public String getToken() {
    return token;
  }

  public void setToken(final String token) {
    this.token = token;
  }
}
