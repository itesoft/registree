package com.itesoft.registree.rest;

import static com.itesoft.registree.rest.test.Constants.API_URL_PREFIX;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.itesoft.registree.dto.ApiError;
import com.itesoft.registree.dto.CreateTokenArgs;
import com.itesoft.registree.dto.CreateTokenResult;
import com.itesoft.registree.dto.CreateUserArgs;
import com.itesoft.registree.dto.UpdateUserArgs;
import com.itesoft.registree.dto.UpdateUserPasswordArgs;
import com.itesoft.registree.dto.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class UserRestControllerTest extends RestControllerTest {
  private static final String USERS_URL = API_URL_PREFIX + "/users";

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void getAdminUser() throws Exception {
    final String username = "admin";
    final User user = restTemplate.getForObject(server + USERS_URL + "/" + username,
                                                User.class);
    assertEquals(username, user.getUsername());
  }

  @Test
  public void createUser() throws Exception {
    final String username = "createUser";
    final String password = "pass";

    final CreateUserArgs createUserArgs = CreateUserArgs.builder().username(username).password(password).build();
    User user =
      restTemplate.postForObject(server + USERS_URL,
                                 createUserArgs,
                                 User.class);
    assertEquals(username, user.getUsername());

    user = restTemplate.getForObject(server + USERS_URL + "/" + user.getId(),
                                     User.class);
    assertEquals(username, user.getUsername());

    user = restTemplate.getForObject(server + USERS_URL + "/" + user.getUsername(),
                                     User.class);
    assertEquals(username, user.getUsername());
  }

  @Test
  public void createSameUserTwice() throws Exception {
    final String username = "createSameUserTwice";
    final String password = "pass";

    final CreateUserArgs createUserArgs = CreateUserArgs.builder().username(username).password(password).build();
    final User user =
      restTemplate.postForObject(server + USERS_URL,
                                 createUserArgs,
                                 User.class);
    assertEquals(username, user.getUsername());

    final ResponseEntity<ApiError> errorResponse =
      restTemplate.postForEntity(server + USERS_URL,
                                 createUserArgs,
                                 ApiError.class);
    assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  public void updateUser() throws Exception {
    final String username = "updateUser";
    final String otherUsername = "updateUser_other";
    final String password = "pass";

    final CreateUserArgs createUserArgs = CreateUserArgs.builder().username(username).password(password).build();
    User user =
      restTemplate.postForObject(server + USERS_URL,
                                 createUserArgs,
                                 User.class);
    assertEquals(username, user.getUsername());

    final UpdateUserArgs updateUserArgs = UpdateUserArgs.builder().username(otherUsername).build();
    final HttpEntity<UpdateUserArgs> putEntity = new HttpEntity<>(updateUserArgs);

    final ResponseEntity<User> responseEntity =
      restTemplate.exchange(server + USERS_URL + "/" + user.getId(),
                            HttpMethod.PUT,
                            putEntity,
                            User.class);
    user = responseEntity.getBody();
    assertEquals(otherUsername, user.getUsername());

    user = restTemplate.getForObject(server + USERS_URL + "/" + user.getId(),
                                     User.class);
    assertEquals(otherUsername, user.getUsername());

    user = restTemplate.getForObject(server + USERS_URL + "/" + user.getUsername(),
                                     User.class);
    assertEquals(otherUsername, user.getUsername());

    final ResponseEntity<ApiError> errorResponse =
      restTemplate.getForEntity(server + USERS_URL + "/" + username,
                                ApiError.class);
    assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void updateUserToExistingUser() throws Exception {
    final String username = "updateUserToExistingUser";
    final String existingUsername = "updateUserToExistingUser_existing";
    final String password = "pass";

    CreateUserArgs createUserArgs = CreateUserArgs.builder().username(existingUsername).password(password).build();
    User user =
      restTemplate.postForObject(server + USERS_URL,
                                 createUserArgs,
                                 User.class);
    assertEquals(existingUsername, user.getUsername());

    createUserArgs = CreateUserArgs.builder().username(username).password(password).build();
    user =
      restTemplate.postForObject(server + USERS_URL,
                                 createUserArgs,
                                 User.class);
    assertEquals(username, user.getUsername());

    final UpdateUserArgs updateUserArgs = UpdateUserArgs.builder().username(existingUsername).build();
    final HttpEntity<UpdateUserArgs> putEntity = new HttpEntity<>(updateUserArgs);

    final ResponseEntity<ApiError> errorResponse =
      restTemplate.exchange(server + USERS_URL + "/" + user.getId(),
                            HttpMethod.PUT,
                            putEntity,
                            ApiError.class);
    assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  public void updateAdminFails() throws Exception {
    final String username = "admin";
    final String otherUsername = "admin_other";

    final UpdateUserArgs updateUserArgs = UpdateUserArgs.builder().username(otherUsername).build();
    final HttpEntity<UpdateUserArgs> putEntity = new HttpEntity<>(updateUserArgs);

    final ResponseEntity<ApiError> errorResponse =
      restTemplate.exchange(server + USERS_URL + "/" + username,
                            HttpMethod.PUT,
                            putEntity,
                            ApiError.class);
    assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void updateUserPassword() throws Exception {
    final String username = "updateUserPassword";
    final String password = "pass";
    final String newPassword = "new";

    final CreateUserArgs createUserArgs = CreateUserArgs.builder().username(username).password(password).build();
    final User user =
      restTemplate.postForObject(server + USERS_URL,
                                 createUserArgs,
                                 User.class);
    assertEquals(username, user.getUsername());

    updatePassword(username, password, newPassword);
  }

  @Test
  public void updateAdminPassword() throws Exception {
    final String username = "admin";
    final String password = "admin";
    final String newPassword = "new";

    updatePassword(username, password, newPassword);
  }

  @Test
  public void deleteUser() throws Exception {
    final String username = "deleteUser";
    final String password = "pass";

    final CreateUserArgs createUserArgs = CreateUserArgs.builder().username(username).password(password).build();
    User user =
      restTemplate.postForObject(server + USERS_URL,
                                 createUserArgs,
                                 User.class);
    assertEquals(username, user.getUsername());

    user = restTemplate.getForObject(server + USERS_URL + "/" + user.getId(),
                                     User.class);
    assertEquals(username, user.getUsername());

    restTemplate.delete(server + USERS_URL + "/" + user.getId());

    final ResponseEntity<ApiError> errorResponse =
      restTemplate.getForEntity(server + USERS_URL + "/" + username,
                                ApiError.class);
    assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  private void updatePassword(final String username,
                              final String password,
                              final String newPassword) {
    CreateTokenArgs createTokenArgs = CreateTokenArgs.builder().username(username).password(password).build();
    CreateTokenResult createTokenResult =
      restTemplate.postForObject(server + TOKENS_URL,
                                 createTokenArgs,
                                 CreateTokenResult.class);
    assertNotNull(createTokenResult);

    createTokenArgs = CreateTokenArgs.builder().username(username).password(newPassword).build();
    ResponseEntity<ApiError> errorResponse =
        restTemplate.exchange(server + TOKENS_URL,
                              HttpMethod.POST,
                              new HttpEntity<>(createTokenArgs),
                              ApiError.class);
    assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    final UpdateUserPasswordArgs updateUserPasswordArgs = UpdateUserPasswordArgs.builder().newPassword(newPassword).build();
    final HttpEntity<UpdateUserPasswordArgs> putEntity = new HttpEntity<>(updateUserPasswordArgs);

    final ResponseEntity<Void> response =
      restTemplate.exchange(server + USERS_URL + "/" + username + "/password",
                            HttpMethod.PUT,
                            putEntity,
                            Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    createTokenArgs = CreateTokenArgs.builder().username(username).password(password).build();
    errorResponse =
        restTemplate.exchange(server + TOKENS_URL,
                              HttpMethod.POST,
                              new HttpEntity<>(createTokenArgs),
                              ApiError.class);
    assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    createTokenArgs = CreateTokenArgs.builder().username(username).password(newPassword).build();
    createTokenResult =
      restTemplate.postForObject(server + TOKENS_URL,
                                 createTokenArgs,
                                 CreateTokenResult.class);
    assertNotNull(createTokenResult);
  }
}
