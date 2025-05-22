package com.itesoft.registree.oci.dto.json;

import java.util.List;

public class ErrorsDto {
  private List<ErrorDto> errors;

  public List<ErrorDto> getErrors() {
    return errors;
  }

  public void setErrors(final List<ErrorDto> errors) {
    this.errors = errors;
  }
}
