package com.itesoft.registree.rest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.UserController;
import com.itesoft.registree.dto.CreateTokenArgs;
import com.itesoft.registree.dto.CreateTokenResult;
import com.itesoft.registree.dto.CreateUserArgs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

public class TokenRestControllerTest extends RestControllerTest {
  @LocalServerPort
  private int port;

  @Autowired
  private UserController userController;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void createAndDeleteToken() throws Exception {
    final String username = "createToken";
    final String password = "pass";

    final CreateUserArgs createUserArgs = CreateUserArgs.builder().username(username).password(password).build();
    userController.createUser(RequestContext.builder().build(),
                              ResponseContext.builder().build(),
                              createUserArgs);

    final CreateTokenArgs createTokenArgs = CreateTokenArgs.builder().username(username).password(password).build();
    final CreateTokenResult createTokenResult =
      restTemplate.postForObject(server + TOKENS_URL,
                                 createTokenArgs,
                                 CreateTokenResult.class);
    assertNotNull(createTokenResult);
    final String token = createTokenResult.getToken();
    assertNotNull(token);

    restTemplate.delete(server + TOKENS_URL + "/" + token);
  }
}
