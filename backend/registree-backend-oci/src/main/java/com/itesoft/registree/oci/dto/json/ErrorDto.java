package com.itesoft.registree.oci.dto.json;

public class ErrorDto {
  private String code;
  private String message;

  public String getCode() {
    return code;
  }

  public void setCode(final String code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }
}
