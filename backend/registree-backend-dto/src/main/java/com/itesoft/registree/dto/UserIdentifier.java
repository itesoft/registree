package com.itesoft.registree.dto;

public class UserIdentifier {
  public static class Builder {
    private Long id;
    private String username;

    public Builder() {
    }

    public Builder id(final Long id) {
      this.id = id;

      return this;
    }

    public Builder username(final String username) {
      this.username = username;

      return this;
    }

    public UserIdentifier build() {
      return new UserIdentifier(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private Long id;
  private String username;

  public UserIdentifier() {
  }

  public UserIdentifier(final Builder builder) {
    this.id = builder.id;
    this.username = builder.username;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }
}
