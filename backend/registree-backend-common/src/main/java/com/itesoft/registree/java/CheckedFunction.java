package com.itesoft.registree.java;

@FunctionalInterface
public interface CheckedFunction<T, R> {
  R apply(T t) throws Exception;
}
