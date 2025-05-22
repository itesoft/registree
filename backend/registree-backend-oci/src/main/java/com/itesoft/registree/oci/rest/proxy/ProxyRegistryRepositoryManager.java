package com.itesoft.registree.oci.rest.proxy;

import static com.itesoft.registree.oci.rest.proxy.ProxyHelper.getRemoteName;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.oci.config.OciProxyRegistry;
import com.itesoft.registree.oci.dto.json.RepositoryTagsDto;
import com.itesoft.registree.oci.rest.AbstractRegistryRepositoryManager;
import com.itesoft.registree.oci.rest.OciOperationContext;
import com.itesoft.registree.oci.rest.OciRegistryRepositoryManager;
import com.itesoft.registree.oci.rest.error.ErrorCode;
import com.itesoft.registree.oci.rest.error.OciErrorManager;
import com.itesoft.registree.oci.rest.proxy.auth.OciProxyAuthenticationManager;
import com.itesoft.registree.proxy.HttpHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class ProxyRegistryRepositoryManager extends AbstractRegistryRepositoryManager implements OciRegistryRepositoryManager {
  private static final String GET_TAGS_LIST_URL = "%s/v2/%s/tags/list";

  private static final Logger LOGGER = LoggerFactory.getLogger(ProxyRegistryRepositoryManager.class);

  @Autowired
  private OciErrorManager errorManager;

  @Autowired
  private OciProxyAuthenticationManager proxyAuthenticationManager;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private HttpHelper httpHelper;

  @Override
  public RegistryType getType() {
    return RegistryType.PROXY;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getCatalog(final OciOperationContext context,
                                                          final HttpServletRequest request)
    throws Exception {
    return getCatalog(context);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getTags(final OciOperationContext context,
                                                       final HttpServletRequest request,
                                                       final String name)
    throws Exception {
    // TODO: add some ping to remove host, if not ok, use local data

    final String remoteName = getRemoteName(name);

    final OciProxyRegistry proxyRegistry = (OciProxyRegistry) context.getRegistry();

    final URIBuilder uriBuilder =
      new URIBuilder(String.format(GET_TAGS_LIST_URL,
                                   proxyRegistry.getProxyUrl(),
                                   remoteName));
    final URI uri = uriBuilder.build();
    final HttpGet httpGet = new HttpGet(uri);
    // TODO: add accept
    final boolean authenticated =
      proxyAuthenticationManager.addAuthentication(httpGet,
                                                   proxyRegistry,
                                                   remoteName);
    if (!authenticated) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .build();
    }

    final List<String> remoteTags;
    try (CloseableHttpClient httpClient = httpHelper.createHttpClient()) {
      remoteTags = httpClient.execute(httpGet, proxyResponse -> {
        final HttpEntity entity = proxyResponse.getEntity();

        if (HttpStatus.OK.value() != proxyResponse.getCode()) {
          LOGGER.error("[{}] Proxy answered with code {} when getting tags of {}",
                       proxyRegistry.getName(),
                       proxyResponse.getCode(),
                       name);
          return null;
        }

        final RepositoryTagsDto repositoryTagsDto =
          objectMapper.readValue(entity.getContent(), RepositoryTagsDto.class);
        return repositoryTagsDto.getTags();
      });
    }

    final RepositoryTagsDto repositoryTags = new RepositoryTagsDto();
    repositoryTags.setName(name);

    final List<String> localTags = getTags(context, name);
    if (localTags == null && remoteTags == null) {
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                           ErrorCode.NAME_UNKNOWN,
                                           String.format("Repository %s not found", name));
    }

    if (localTags != null) {
      final Set<String> allTags = new HashSet<>();
      if (remoteTags != null) {
        allTags.addAll(remoteTags);
      }
      allTags.addAll(localTags);
      final List<String> orderedTags = new ArrayList<>(allTags);
      orderElements(orderedTags);
      repositoryTags.setTags(orderedTags);
    } else {
      repositoryTags.setTags(remoteTags);
    }

    final StreamingResponseBody stream = outputStream -> {
      objectMapper.writeValue(outputStream, repositoryTags);
    };

    return ResponseEntity.ok(stream);
  }
}
