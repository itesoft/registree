package com.itesoft.registree.java;

@FunctionalInterface
public interface CheckedBiFunction<T, U, R> {
  R apply(T t, U u) throws Exception;
}
