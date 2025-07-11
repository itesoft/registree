package com.itesoft.registree.dto;

import jakarta.validation.constraints.NotEmpty;

import org.springframework.validation.annotation.Validated;

@Validated
public class GetTokenResult {
  public static class Builder {
    private String token;

    public Builder() {
      // empty default constructor
    }

    public Builder token(final String token) {
      this.token = token;
      return this;
    }

    public GetTokenResult build() {
      return new GetTokenResult(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @NotEmpty
  private String token;

  public GetTokenResult() {
  }

  public GetTokenResult(final Builder builder) {
    this.token = builder.token;
  }

  public String getToken() {
    return token;
  }

  public void setToken(final String token) {
    this.token = token;
  }
}
