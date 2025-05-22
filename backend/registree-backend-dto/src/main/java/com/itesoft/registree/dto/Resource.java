package com.itesoft.registree.dto;

public class Resource {
  public static class Builder {
    private String name;
    private String path;
    private String parentPath;
    private String type;
    private String relativeDownloadPath;
    private String sourceRegistryName;
    private String componentGav;
    private String filePath;

    public Builder() {
      // empty default constructor
    }

    public Builder name(final String name) {
      this.name = name;
      return this;
    }

    public Builder path(final String path) {
      this.path = path;
      return this;
    }

    public Builder parentPath(final String parentPath) {
      this.parentPath = parentPath;
      return this;
    }

    public Builder type(final String type) {
      this.type = type;
      return this;
    }

    public Builder relativeDownloadPath(final String relativeDownloadPath) {
      this.relativeDownloadPath = relativeDownloadPath;
      return this;
    }

    public Builder sourceRegistryName(final String sourceRegistryName) {
      this.sourceRegistryName = sourceRegistryName;
      return this;
    }

    public Builder componentGav(final String componentGav) {
      this.componentGav = componentGav;
      return this;
    }

    public Builder filePath(final String filePath) {
      this.filePath = filePath;
      return this;
    }

    public Resource build() {
      return new Resource(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private String name;
  private String path;
  private String parentPath;
  private String type;
  private String relativeDownloadPath;
  private String sourceRegistryName;
  private String componentGav;
  private String filePath;

  public Resource() {
  }

  public Resource(final Builder builder) {
    this.name = builder.name;
    this.path = builder.path;
    this.parentPath = builder.parentPath;
    this.type = builder.type;
    this.relativeDownloadPath = builder.relativeDownloadPath;
    this.sourceRegistryName = builder.sourceRegistryName;
    this.componentGav = builder.componentGav;
    this.filePath = builder.filePath;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(final String path) {
    this.path = path;
  }

  public String getParentPath() {
    return parentPath;
  }

  public void setParentPath(final String parentPath) {
    this.parentPath = parentPath;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getRelativeDownloadPath() {
    return relativeDownloadPath;
  }

  public void setRelativeDownloadPath(final String relativeDownloadPath) {
    this.relativeDownloadPath = relativeDownloadPath;
  }

  public String getSourceRegistryName() {
    return sourceRegistryName;
  }

  public void setSourceRegistryName(final String sourceRegistryName) {
    this.sourceRegistryName = sourceRegistryName;
  }

  public String getComponentGav() {
    return componentGav;
  }

  public void setComponentGav(final String componentGav) {
    this.componentGav = componentGav;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(final String filePath) {
    this.filePath = filePath;
  }
}
