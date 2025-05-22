package com.itesoft.registree.oci.storage;

public abstract class OciDigestHelper {
  public static Digest fromString(final String digest) {
    final String[] tab = digest.split(":");
    return new Digest(tab[0], tab[1]);
  }

  private OciDigestHelper() {
  }
}
