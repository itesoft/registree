package com.itesoft.registree.oci.storage;

public class Digest {
  private final String algorithm;
  private final String hex;

  public Digest(final String algorithm, final String hex) {
    this.algorithm = algorithm;
    this.hex = hex;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public String getHex() {
    return hex;
  }
}
