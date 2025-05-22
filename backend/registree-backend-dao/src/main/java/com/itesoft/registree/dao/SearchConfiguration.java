package com.itesoft.registree.dao;

import org.springframework.context.annotation.Configuration;

@Configuration("search")
public class SearchConfiguration {
  private static final int DEFAULT_PAGE_SIZE = 20;

  public int defaultPageSize() {
    return DEFAULT_PAGE_SIZE;
  }
}
