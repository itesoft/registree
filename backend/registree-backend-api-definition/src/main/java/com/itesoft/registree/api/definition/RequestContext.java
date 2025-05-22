package com.itesoft.registree.api.definition;

public class RequestContext { // SUPPRESS CHECKSTYLE HideUtilityClassConstructor
  /**
   * Builder for RequestContext
   */
  public static class Builder {
    /**
     * Default constructor
     */
    public Builder() {
    }

    /**
     * Build a new instance of RequestContext
     *
     * @return a new instance of RequestContext
     */
    public RequestContext build() {
      return new RequestContext(this);
    }
  }

  /**
   * Returns a new builder
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Default constructor
   */
  public RequestContext() {
  }

  /**
   * Constructor with builder
   *
   * @param builder the builder used to defined this object instance
   */
  public RequestContext(final Builder builder) {
  }
}
