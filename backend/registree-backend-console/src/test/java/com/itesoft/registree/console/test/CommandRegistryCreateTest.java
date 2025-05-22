package com.itesoft.registree.console.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.CommandController;
import com.itesoft.registree.console.dto.ExecuteCommandResult;
import com.itesoft.registree.controller.RegistryController;
import com.itesoft.registree.dto.Registry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CommandRegistryCreateTest {
  @Autowired
  private CommandController commandController;

  @Autowired
  private RegistryController registryController;

  @Test
  public void createRegistry() throws Exception {
    final String format = "format";
    final String type = "hosted";
    final String name = "createRegistry";

    final ExecuteCommandResult executeCommandResult = commandController.execute(RequestContext.builder().build(),
                                                                                ResponseContext.builder().build(),
                                                                                "registry-create",
                                                                                format,
                                                                                type,
                                                                                name);
    assertEquals(0, executeCommandResult.getExitCode());
    final Registry registry =
      registryController.getRegistry(RequestContext.builder().build(),
                                     ResponseContext.builder().build(),
                                     name);
    assertEquals(format, registry.getFormat());
    assertEquals(type, registry.getType());
    assertEquals(name, registry.getName());
    assertEquals("{}", registry.getConfiguration());
  }
}
