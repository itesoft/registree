package com.itesoft.registree.java;

@FunctionalInterface
public interface CheckedRunnable {
  void run() throws Exception;
}
