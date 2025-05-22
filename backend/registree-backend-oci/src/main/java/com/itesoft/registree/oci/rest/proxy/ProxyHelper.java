package com.itesoft.registree.oci.rest.proxy;

abstract class ProxyHelper {
  static String getRemoteName(final String name) {
    if (name.contains("/")) {
      return name;
    }
    return "library/" + name;
  }

  private ProxyHelper() {
  }
}
