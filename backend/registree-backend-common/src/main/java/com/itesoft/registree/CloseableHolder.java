package com.itesoft.registree;

import java.io.Closeable;

public class CloseableHolder {
  private final Closeable closeable;
  private long lastUsed;

  public CloseableHolder(final Closeable closeable) {
    this.closeable = closeable;
    this.lastUsed = System.currentTimeMillis();
  }

  public Closeable getCloseable() {
    return closeable;
  }

  public long getLastUsed() {
    return lastUsed;
  }

  public void setLastUsed(final long lastUsed) {
    this.lastUsed = lastUsed;
  }

}
