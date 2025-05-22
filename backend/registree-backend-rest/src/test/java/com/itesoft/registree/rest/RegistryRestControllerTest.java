package com.itesoft.registree.rest;

import static com.itesoft.registree.rest.test.Constants.API_URL_PREFIX;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.itesoft.registree.dto.ApiError;
import com.itesoft.registree.dto.CreateRegistryArgs;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.dto.UpdateRegistryArgs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RegistryRestControllerTest extends RestControllerTest {
  private static final String REGISTRIES_URL = API_URL_PREFIX + "/registries";

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void createRegistry() throws Exception {
    final String name = "createRegistry";
    final String format = "custom";
    final String configuration = "{}";

    final CreateRegistryArgs createRegistryArgs = CreateRegistryArgs.builder()
      .name(name)
      .format(format)
      .type(RegistryType.HOSTED.name())
      .configuration(configuration)
      .build();
    Registry registry =
      restTemplate.postForObject(server + REGISTRIES_URL,
                                 createRegistryArgs,
                                 Registry.class);
    assertEquals(name, registry.getName());
    assertEquals(format, registry.getFormat());
    assertEquals(configuration, registry.getConfiguration());

    registry = restTemplate.getForObject(server + REGISTRIES_URL + "/" + name,
                                         Registry.class);
    assertEquals(name, registry.getName());
    assertEquals(format, registry.getFormat());
    assertEquals(RegistryType.HOSTED.getValue(), registry.getType());
    assertEquals(configuration, registry.getConfiguration());
  }

  @Test
  public void createSameRegistryTwice() throws Exception {
    final String name = "createSameRegistryTwice";
    final String format = "custom";
    final String configuration = "{}";

    final CreateRegistryArgs createRegistryArgs = CreateRegistryArgs.builder()
      .name(name)
      .format(format)
      .type(RegistryType.HOSTED.name())
      .configuration(configuration)
      .build();
    final Registry registry =
      restTemplate.postForObject(server + REGISTRIES_URL,
                                 createRegistryArgs,
                                 Registry.class);
    assertEquals(name, registry.getName());
    assertEquals(format, registry.getFormat());
    assertEquals(RegistryType.HOSTED.getValue(), registry.getType());
    assertEquals(configuration, registry.getConfiguration());

    final ResponseEntity<ApiError> errorResponse =
      restTemplate.postForEntity(server + REGISTRIES_URL,
                                 createRegistryArgs,
                                 ApiError.class);
    assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  public void updateRegistry() throws Exception {
    final String name = "updateRegistry";
    final String format = "custom";
    final String configuration = "{}";
    final String configurationOther = "{\"test\": \"value\"}";

    final CreateRegistryArgs createRegistryArgs = CreateRegistryArgs.builder()
      .name(name)
      .format(format)
      .type(RegistryType.HOSTED.name())
      .configuration(configuration)
      .build();
    Registry registry =
      restTemplate.postForObject(server + REGISTRIES_URL,
                                 createRegistryArgs,
                                 Registry.class);
    assertEquals(name, registry.getName());
    assertEquals(format, registry.getFormat());
    assertEquals(RegistryType.HOSTED.getValue(), registry.getType());
    assertEquals(configuration, registry.getConfiguration());

    final UpdateRegistryArgs updateRegistryArgs = UpdateRegistryArgs.builder()
      .configuration(configurationOther)
      .build();
    final HttpEntity<UpdateRegistryArgs> putEntity = new HttpEntity<>(updateRegistryArgs);

    final ResponseEntity<Registry> responseEntity =
      restTemplate.exchange(server + REGISTRIES_URL + "/" + name,
                            HttpMethod.PUT,
                            putEntity,
                            Registry.class);
    registry = responseEntity.getBody();
    assertEquals(name, registry.getName());
    assertEquals(format, registry.getFormat());
    assertEquals(RegistryType.HOSTED.getValue(), registry.getType());
    assertEquals(configurationOther, registry.getConfiguration());

    registry = restTemplate.getForObject(server + REGISTRIES_URL + "/" + name,
                                         Registry.class);
    assertEquals(name, registry.getName());
    assertEquals(format, registry.getFormat());
    assertEquals(RegistryType.HOSTED.getValue(), registry.getType());
    assertEquals(configurationOther, registry.getConfiguration());
  }

  @Test
  public void deleteRegistry() throws Exception {
    final String name = "deleteRegistry";
    final String format = "custom";
    final String configuration = "{}";

    final CreateRegistryArgs createRegistryArgs = CreateRegistryArgs.builder()
      .name(name)
      .format(format)
      .type(RegistryType.HOSTED.name())
      .configuration(configuration)
      .build();
    Registry registry =
      restTemplate.postForObject(server + REGISTRIES_URL,
                                 createRegistryArgs,
                                 Registry.class);
    assertEquals(name, registry.getName());
    assertEquals(format, registry.getFormat());
    assertEquals(RegistryType.HOSTED.getValue(), registry.getType());
    assertEquals(configuration, registry.getConfiguration());

    registry = restTemplate.getForObject(server + REGISTRIES_URL + "/" + name,
                                         Registry.class);
    assertEquals(name, registry.getName());
    assertEquals(format, registry.getFormat());
    assertEquals(configuration, registry.getConfiguration());

    restTemplate.delete(server + REGISTRIES_URL + "/" + name);

    final ResponseEntity<ApiError> errorResponse =
      restTemplate.getForEntity(server + REGISTRIES_URL + "/" + name,
                                ApiError.class);
    assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
