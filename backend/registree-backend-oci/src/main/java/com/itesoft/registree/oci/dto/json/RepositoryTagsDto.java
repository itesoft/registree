package com.itesoft.registree.oci.dto.json;

import java.util.List;

public class RepositoryTagsDto {
  private String name;
  private List<String> tags;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(final List<String> tags) {
    this.tags = tags;
  }
}
