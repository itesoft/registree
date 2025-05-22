package com.itesoft.registree.java;

@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception> {
  void accept(T t) throws E;
}
