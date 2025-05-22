package com.itesoft.registree.npm.dto.json;

public class ErrorDto extends ResponseDto {
  private String error;

  public String getError() {
    return error;
  }

  public void setError(final String error) {
    this.error = error;
  }

  @Override
  public boolean isSuccess() {
    return false;
  }
}
