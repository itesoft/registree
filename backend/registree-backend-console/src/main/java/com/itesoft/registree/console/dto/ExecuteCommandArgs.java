package com.itesoft.registree.console.dto;

import java.util.List;

public class ExecuteCommandArgs {
  public static class Builder {
    private String command;
    private List<String> arguments;

    public Builder() {
      // empty default constructor
    }

    public Builder command(final String command) {
      this.command = command;
      return this;
    }

    public Builder output(final List<String> arguments) {
      this.arguments = arguments;
      return this;
    }

    public ExecuteCommandArgs build() {
      return new ExecuteCommandArgs(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private String command;
  private List<String> arguments;

  public ExecuteCommandArgs() {
  }

  public ExecuteCommandArgs(final Builder builder) {
    this.command = builder.command;
    this.arguments = builder.arguments;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(final String command) {
    this.command = command;
  }

  public List<String> getArguments() {
    return arguments;
  }

  public void setArguments(final List<String> arguments) {
    this.arguments = arguments;
  }
}
