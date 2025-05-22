package com.itesoft.registree.console.command.user;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.controller.UserController;
import com.itesoft.registree.dto.CreateUserArgs;
import com.itesoft.registree.dto.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "user-create",
         description = "Creates a user")
public class UserCreateCommand implements RegistreeCommand {
  @Parameters(index = "0",
              description = "The username")
  private String username;

  @Parameters(index = "1",
              description = "The password")
  private String password;

  @Option(names = "-f",
          description = "The user first name")
  private String firstName;

  @Option(names = "-l",
          description = "The user last name")
  private String lastName;

  @Spec
  private CommandSpec spec;

  @Autowired
  private UserController userController;

  @Override
  public Integer call() throws Exception {
    final CreateUserArgs createUserArgs =
      CreateUserArgs.builder()
        .username(username)
        .password(password)
        .firstName(firstName)
        .lastName(lastName)
        .build();
    final User user =
      userController.createUser(RequestContext.builder().build(),
                                ResponseContext.builder().build(),
                                createUserArgs);
    spec.commandLine().getOut().println(String.format("User created, got id '%s'", user.getId()));
    return 0;
  }
}
