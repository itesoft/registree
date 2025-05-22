package com.itesoft.registree.npm.dto;

import com.itesoft.registree.npm.dto.json.ResponsePackage;

public class GetPackageResult {
  public static class Builder {
    private ResponsePackage responsePackage;
    private String checksum;

    public Builder() {
      // empty default constructor
    }

    public Builder responsePackage(final ResponsePackage responsePackage) {
      this.responsePackage = responsePackage;
      return this;
    }

    public Builder checksum(final String checksum) {
      this.checksum = checksum;
      return this;
    }

    public GetPackageResult build() {
      return new GetPackageResult(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private ResponsePackage responsePackage;
  private String checksum;

  public GetPackageResult() {
  }

  public GetPackageResult(final Builder builder) {
    this.responsePackage = builder.responsePackage;
    this.checksum = builder.checksum;
  }

  public ResponsePackage getResponsePackage() {
    return responsePackage;
  }

  public void setResponsePackage(final ResponsePackage responsePackage) {
    this.responsePackage = responsePackage;
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(final String checksum) {
    this.checksum = checksum;
  }
}
