package com.itesoft.registree;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IoHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(IoHelper.class);

  public static void deleteSilently(final Path file) {
    try {
      Files.deleteIfExists(file);
    } catch (final IOException exception) {
      LOGGER.warn(exception.getMessage(), exception);
    }
  }

  public static void closeSilently(final Closeable closeable) {
    try {
      closeable.close();
    } catch (final IOException exception) {
      LOGGER.warn(exception.getMessage(), exception);
    }
  }
}
