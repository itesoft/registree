package com.itesoft.registree.maven.test.external;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UseExternalDependencyHelloWorld
{
  public static void main(@NotNull @Size(min = 1, max = 1) final String[] args)
  {
    System.out.println("Hello " + args[0]);
  }
}
