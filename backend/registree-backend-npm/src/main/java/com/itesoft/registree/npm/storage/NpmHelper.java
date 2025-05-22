package com.itesoft.registree.npm.storage;

public abstract class NpmHelper {
  public static String extractVersionFromFileName(final String packageName,
                                                  final String fileName) {
    final int beginIndex = (packageName + "-").length();
    final int lastIndex = fileName.lastIndexOf('.');
    return fileName.substring(beginIndex, lastIndex);
  }

  private NpmHelper() {
  }
}
