package com.itesoft.registree.npm.dto.json;

public class OkDto extends ResponseDto {
  public static class Builder {
    private String ok;

    public Builder() {
      // empty default constructor
    }

    public Builder ok(final String ok) {
      this.ok = ok;
      return this;
    }

    public OkDto build() {
      return new OkDto(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private String ok;

  public OkDto() {
  }

  public OkDto(final Builder builder) {
    this.ok = builder.ok;
  }

  public String getOk() {
    return ok;
  }

  public void setOk(final String ok) {
    this.ok = ok;
  }

  @Override
  public boolean isSuccess() {
    return true;
  }
}
