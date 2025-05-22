package com.itesoft.registree.java;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpirableCache<K, V> extends ConcurrentHashMap<K, V> {
  private static final int CLEANUP_DELAY = 10;

  private static final long serialVersionUID = 1L;

  // TODO: use spring scheduling
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private final Map<K, Long> timeMap = new ConcurrentHashMap<>();
  private final long expirationDelay;

  public ExpirableCache(final long expirationDelay) {
    this.expirationDelay = expirationDelay;
    startTask();
  }

  @Override
  public V put(final K key, final V value) {
    final Date date = new Date();
    timeMap.put(key, date.getTime());
    return super.put(key, value);
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> m) {
    final long time = System.currentTimeMillis();
    for (final Entry<? extends K, ? extends V> entry : m.entrySet()) {
      timeMap.put(entry.getKey(), time);
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public V putIfAbsent(final K key, final V value) {
    if (!containsKey(key)) {
      timeMap.put(key, System.currentTimeMillis());
      return put(key, value);
    } else {
      return get(key);
    }
  }

  @Override
  public V remove(final Object key) {
    timeMap.remove(key);
    return super.remove(key);
  }

  @Override
  public boolean remove(final Object key, final Object value) {
    final boolean removed = super.remove(key, value);
    if (removed) {
      timeMap.remove(key);
    }
    return removed;
  }

  public void refresh(final K key) {
    if (containsKey(key)) {
      timeMap.put(key, System.currentTimeMillis());
    }
  }

  private void startTask() {
    final Runnable cleanup = new Runnable() {
      @Override
      public void run() {
        final long currentTime = System.currentTimeMillis();
        final Iterator<Entry<K, Long>> iter = timeMap.entrySet().iterator();
        while (iter.hasNext()) {
          final Entry<K, Long> entry = iter.next();
          if (currentTime > (entry.getValue() + expirationDelay)) {
            remove(entry.getKey());
            iter.remove();
          }
        }
      }
    };
    executor.scheduleWithFixedDelay(cleanup, CLEANUP_DELAY, CLEANUP_DELAY, TimeUnit.SECONDS);
  }
}
