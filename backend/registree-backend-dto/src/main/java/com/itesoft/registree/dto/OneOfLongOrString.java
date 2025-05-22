package com.itesoft.registree.dto;

/**
 * Represents one of long or String value
 */
public class OneOfLongOrString {
  /**
   * Create a OneOfLongOrString using a given String. This method tries to create
   * a long value and if the given String is not a long, creates the String form.
   *
   * @param value the value to parse
   * @return the newly created OneOf
   */
  public static OneOfLongOrString fromString(final String value) {
    try {
      return new OneOfLongOrString(Long.parseLong(value));
    } catch (final NumberFormatException exception) {
      return new OneOfLongOrString(value);
    }
  }

  /**
   * Alias of {@link #fromString(String)}
   *
   * @param value the value to parse
   * @return the newly created OneOf
   */
  public static OneOfLongOrString valueOf(final String value) {
    return fromString(value);
  }

  /**
   * Create a OneOfLongOrString with a long value
   *
   * @param value the long value
   * @return the newly created OneOfLongOrString
   */
  public static OneOfLongOrString from(final long value) {
    return new OneOfLongOrString(value);
  }

  /**
   * Create a OneOfLongOrString with a String value
   *
   * @param value the String value
   * @return the newly created OneOfLongOrString
   */
  public static OneOfLongOrString from(final String value) {
    return new OneOfLongOrString(value);
  }

  /**
   * Flag indicating if this OneOf is a long value
   */
  private boolean isLong;
  /**
   * Flag indicating if this OneOf is a String value
   */
  private boolean isString;
  /**
   * The long value
   */
  private long longValue;
  /**
   * The String value
   */
  private String stringValue;

  /**
   * Default constructor
   */
  public OneOfLongOrString() {
  }

  private OneOfLongOrString(final long value) {
    longValue = value;
    isLong = true;
  }

  private OneOfLongOrString(final String value) {
    stringValue = value;
    isString = true;
  }

  /**
   * Returns <code>true</code> if this OneOf is long value, <code>false</code>
   * otherwise
   *
   * @return <code>true</code> if this OneOf is long value, <code>false</code>
   *         otherwise
   */
  public boolean isLong() {
    return isLong;
  }

  /**
   * Sets the flag indicating the value if a long of not. No intended to be called
   * directly.
   *
   * @param flag the boolean indicating if the value is a long
   */
  public void setLong(final boolean flag) {
    isLong = flag;
  }

  /**
   * Returns <code>true</code> if this OneOf is String value, <code>false</code>
   * otherwise
   *
   * @return <code>true</code> if this OneOf is String value, <code>false</code>
   *         otherwise
   */
  public boolean isString() {
    return isString;
  }

  /**
   * Sets the flag indicating the value if a String of not. No intended to be
   * called directly.
   *
   * @param flag the boolean indicating if the value is a String
   */
  public void setString(final boolean flag) {
    isString = flag;
  }

  /**
   * Returns the long value, check if it is a long value first, using
   * {@link #isLong()}
   *
   * @return the long value
   */
  public long getLongValue() {
    return longValue;
  }

  /**
   * Sets this OneOf as a long value
   *
   * @param longValue the long value
   */
  public void setLongValue(final long longValue) {
    this.longValue = longValue;
  }

  /**
   * Returns the String value, check if it is a String value first, using
   * {@link #isString()}
   *
   * @return the String value
   */
  public String getStringValue() {
    return stringValue;
  }

  /**
   * Sets this OneOf as a String value
   *
   * @param stringValue the String value
   */
  public void setStringValue(final String stringValue) {
    this.stringValue = stringValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    if (isLong) {
      return Long.toString(longValue);
    } else {
      return stringValue;
    }
  }
}
