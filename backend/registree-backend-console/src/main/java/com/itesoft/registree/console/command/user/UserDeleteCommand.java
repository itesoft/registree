package com.itesoft.registree.console.command.user;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.controller.UserController;
import com.itesoft.registree.dto.OneOfLongOrString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "user-delete",
         description = "Deletes a user")
public class UserDeleteCommand implements RegistreeCommand {
  @Parameters(index = "0",
              description = "The id or name of the user")
  private String idOrName;

  @Spec
  private CommandSpec spec;

  @Autowired
  private UserController userController;

  @Override
  public Integer call() throws Exception {
    userController.deleteUser(RequestContext.builder().build(),
                              ResponseContext.builder().build(),
                              OneOfLongOrString.valueOf(idOrName),
                              null);
    spec.commandLine().getOut().println("User deleted");
    return 0;
  }
}
