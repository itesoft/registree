package com.itesoft.registree.dto;

public class ApiError {
  public static class Builder {
    private String message;

    public Builder() {
      // empty default constructor
    }

    public Builder message(final String message) {
      this.message = message;
      return this;
    }

    public ApiError build() {
      return new ApiError(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public ApiError() {
  }

  public ApiError(final Builder builder) {
    this.message = builder.message;
  }

  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

}
