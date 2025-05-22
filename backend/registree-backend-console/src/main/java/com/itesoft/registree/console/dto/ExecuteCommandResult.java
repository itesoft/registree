package com.itesoft.registree.console.dto;

public class ExecuteCommandResult {
  public static class Builder {
    private int exitCode;
    private String output;

    public Builder() {
      // empty default constructor
    }

    public Builder exitCode(final int exitCode) {
      this.exitCode = exitCode;
      return this;
    }

    public Builder output(final String output) {
      this.output = output;
      return this;
    }

    public ExecuteCommandResult build() {
      return new ExecuteCommandResult(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private int exitCode;
  private String output;

  public ExecuteCommandResult() {
  }

  public ExecuteCommandResult(final Builder builder) {
    this.exitCode = builder.exitCode;
    this.output = builder.output;
  }

  public int getExitCode() {
    return exitCode;
  }

  public void setExitCode(final int exitCode) {
    this.exitCode = exitCode;
  }

  public String getOutput() {
    return output;
  }

  public void setOutput(final String output) {
    this.output = output;
  }
}
