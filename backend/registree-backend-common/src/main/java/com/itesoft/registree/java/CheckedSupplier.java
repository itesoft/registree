package com.itesoft.registree.java;

@FunctionalInterface
public interface CheckedSupplier<T, E extends Exception> {
  T get() throws E;
}
