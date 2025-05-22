package com.itesoft.registree.oci.dto.json;

import java.util.List;

public class CatalogDto {
  private List<String> repositories;

  public List<String> getRepositories() {
    return repositories;
  }

  public void setRepositories(final List<String> repositories) {
    this.repositories = repositories;
  }
}
