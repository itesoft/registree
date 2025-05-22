package com.itesoft.registree.java;

public abstract class DigestHelper {
  public static String bytesToHex(final byte[] hash) {
    final StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (int i = 0; i < hash.length; i++) {
      final String hex = Integer.toHexString(0xff & hash[i]);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  private DigestHelper() {
  }
}
