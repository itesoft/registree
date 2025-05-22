package com.itesoft.registree.console.command.user;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.controller.UserController;
import com.itesoft.registree.dto.OneOfLongOrString;
import com.itesoft.registree.dto.UpdateUserPasswordArgs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "user-update-password",
         description = "Updates a user's password")
public class UserChangePasswordCommand implements RegistreeCommand {
  @Parameters(index = "0",
              description = "The username")
  private String username;

  @Parameters(index = "1",
              description = "The password")
  private String password;

  @Spec
  private CommandSpec spec;

  @Autowired
  private UserController userController;

  @Override
  public Integer call() throws Exception {
    final UpdateUserPasswordArgs updateUserPasswordArgs =
      UpdateUserPasswordArgs.builder()
        .newPassword(password)
        .build();
    userController.updateUserPassword(RequestContext.builder().build(),
                                      ResponseContext.builder().build(),
                                      OneOfLongOrString.from(username),
                                      updateUserPasswordArgs);
    spec.commandLine().getOut().println("User's password updated, got id '%s'");
    return 0;
  }
}
