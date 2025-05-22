package com.itesoft.registree.acl;

public enum Permission {
  READ("r"),
  WRITE("w"),
  DELETE("d");

  private final String c;

  Permission(final String c) {
    this.c = c;
  }

  String toChar() {
    return c;
  }
}
