package com.itesoft.registree.persistence;

public abstract class WellKnownRoutePaths {
  public static final String ROOT_ROUTE_PATH = "/";
  public static final String API_V1_ROUTE_PATH = "/api/v1";
  public static final String API_V1_TOKENS_ROUTE_PATH = API_V1_ROUTE_PATH + "/tokens";

  private WellKnownRoutePaths() {
  }
}
