package com.itesoft.registree;

import java.util.Random;

public abstract class RandomHelper {
  private static final char[] ALPHANUM = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

  private static final Random RANDOM = new Random();

  public static String random(final int length) {
    final char[] buffer = new char[length];
    for (int i = 0; i < buffer.length; i++) {
      final int index = RANDOM.nextInt(ALPHANUM.length);
      buffer[i] = ALPHANUM[index];
    }
    return new String(buffer);
  }

  private RandomHelper() {
  }
}
