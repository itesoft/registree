package com.itesoft.registree.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({ @Type(value = HostedRegistry.class, name = "hosted"), @Type(value = ProxyRegistry.class, name = "proxy"),
                @Type(value = GroupRegistry.class, name = "group") })
public class Registry {
  private String name;
  private String format;
  private String type;
  private String configuration;
  @JsonIgnore
  private final Map<String, Object> properties = new HashMap<>();

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(final String format) {
    this.format = format;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(final String configuration) {
    this.configuration = configuration;
  }

  @SuppressWarnings("unchecked")
  public <T> T getProperty(final String key) {
    return (T) properties.get(key);
  }

  public void setProperty(final String key, final Object value) {
    properties.put(key, value);
  }
}
