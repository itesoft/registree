package com.itesoft.registree;

import java.io.Closeable;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CloseableCleaner {
  private static final int CLEANUP_DELAY = 60000;

  private static final Logger LOGGER = LoggerFactory.getLogger(CloseableCleaner.class);

  private final Queue<CloseableHolder> closeableHolders = new ConcurrentLinkedQueue<>();

  @Scheduled(fixedDelay = CLEANUP_DELAY)
  public void closeExpired() {
    final long now = System.currentTimeMillis();
    for (final CloseableHolder closeableHolder : closeableHolders) {
      final long lastUsed = closeableHolder.getLastUsed();
      if (lastUsed < now - CLEANUP_DELAY) {
        closeableHolders.remove(closeableHolder);
        final Closeable closeable = closeableHolder.getCloseable();
        try {
          closeable.close();
        } catch (final IOException exception) {
          LOGGER.debug(exception.getMessage(), exception);
        }
      }
    }
  }

  public void add(final CloseableHolder closeableHolder) {
    closeableHolders.add(closeableHolder);
  }

  public void remove(final CloseableHolder closeableHolder) {
    closeableHolders.remove(closeableHolder);
  }
}
