package com.itesoft.registree.api.definition;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a ResponseContext
 */
public class ResponseContext {
  /**
   * Builder for ResponseContext
   */
  public static class Builder {
    private Map<String, Object> extraProperties = new HashMap<>();

    /**
     * Default constructor
     */
    public Builder() {
    }

    /**
     * Sets the extra properties.
     *
     * @param extraProperties the extra properties.
     * @return the builder for convenience
     */
    public Builder extraProperties(final Map<String, Object> extraProperties) {
      this.extraProperties = extraProperties;
      return this;
    }

    /**
     * Build a new instance of ResponseContext
     *
     * @return a new instance of ResponseContext
     */
    public ResponseContext build() {
      return new ResponseContext(this);
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

  private Map<String, Object> extraProperties = new HashMap<>();

  /**
   * Default constructor
   */
  public ResponseContext() {
  }

  /**
   * Constructor with builder
   *
   * @param builder the builder used to defined this object instance
   */
  public ResponseContext(final Builder builder) {

    this.extraProperties = builder.extraProperties;
  }

  /**
   * Returns the extra properties.
   *
   * @return the extra properties.
   */
  public Map<String, Object> getExtraProperties() {
    return this.extraProperties;
  }

  /**
   * Sets the extra properties.
   *
   * @param extraProperties the extra properties.
   */
  public void setExtraProperties(final Map<String, Object> extraProperties) {
    this.extraProperties = extraProperties;
  }
}
