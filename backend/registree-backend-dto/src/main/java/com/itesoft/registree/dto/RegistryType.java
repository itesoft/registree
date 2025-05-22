package com.itesoft.registree.dto;

public enum RegistryType {
  HOSTED("hosted"),

  PROXY("proxy"),

  GROUP("group");

  private final String value;

  RegistryType(final String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
