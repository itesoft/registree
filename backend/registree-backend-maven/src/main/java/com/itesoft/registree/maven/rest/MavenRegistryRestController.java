package com.itesoft.registree.maven.rest;

import static com.itesoft.registree.maven.config.MavenConstants.FORMAT;
import static com.itesoft.registree.maven.config.MavenConstants.METADATA_FILE_NAME;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.acl.AclService;
import com.itesoft.registree.acl.Permission;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.maven.config.MavenRegistries;
import com.itesoft.registree.maven.rest.error.MavenErrorManager;
import com.itesoft.registree.registry.api.RegistryApiRestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class MavenRegistryRestController implements RegistryApiRestController {
  private final Pattern metadataFilePattern = Pattern.compile("^/(.+)/([^/]+)/([^/]+)$");
  private final Pattern artifactFilePattern = Pattern.compile("^/(.+)/([^/]+)/([^/]+)/([^/]+)$");

  @Autowired
  private MavenRegistries mavenRegistries;

  @Autowired
  private MavenErrorManager errorManager;

  @Autowired
  private AclService aclService;

  private final Map<String, MavenMetadataManager> metadataManagers = new HashMap<>();
  private final Map<String, MavenArtifactManager> artifactManagers = new HashMap<>();
  private final Map<String, MavenChecksumManager> checksumManagers = new HashMap<>();

  @Autowired
  public void registerManagers(final List<MavenMetadataManager> metadataManagers,
                               final List<MavenArtifactManager> artifactManagers,
                               final List<MavenChecksumManager> checksumManagers) {
    addManagers(this.metadataManagers, metadataManagers);
    addManagers(this.artifactManagers, artifactManagers);
    addManagers(this.checksumManagers, checksumManagers);
  }

  @Override
  public String getFormat() {
    return FORMAT;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> api(final Registry registry,
                                                   final HttpServletRequest request)
    throws Exception {
    final MavenOperationContext context = new MavenOperationContext(metadataManagers,
                                                                    artifactManagers,
                                                                    checksumManagers,
                                                                    mavenRegistries,
                                                                    registry);

    final String method = request.getMethod();
    final String rawPath = request.getRequestURI();
    final String path = URLDecoder.decode(rawPath, StandardCharsets.UTF_8);

    int lastIndex = path.lastIndexOf("/");
    final String fileName = path.substring(lastIndex + 1);
    lastIndex = fileName.lastIndexOf(".");
    final String extension = fileName.substring(lastIndex + 1);

    boolean isChecksum = false;
    boolean isMetadata = false;
    String groupPath = null;
    String artifactId = null;
    String version = null;
    if (extension.equals("md5")
      || extension.equals("sha1")
      || extension.equals("sha256")
      || extension.equals("sha512")
      || extension.equals("asc")) {
      isChecksum = true;
      if (fileName.startsWith(METADATA_FILE_NAME)) {
        final Matcher matcher = metadataFilePattern.matcher(path);
        if (matcher.matches()) {
          isMetadata = true;
          groupPath = matcher.group(1);
          artifactId = matcher.group(2);
          version = null;
        }
      } else {
        final Matcher matcher = artifactFilePattern.matcher(path);
        if (matcher.matches()) {
          isMetadata = false;
          groupPath = matcher.group(1);
          artifactId = matcher.group(2);
          version = matcher.group(3);
        }
      }
    } else if (fileName.equals(METADATA_FILE_NAME)) {
      final Matcher matcher = metadataFilePattern.matcher(path);
      if (matcher.matches()) {
        isMetadata = true;
        groupPath = matcher.group(1);
        artifactId = matcher.group(2);
        version = null;
      }
    } else {
      final Matcher matcher = artifactFilePattern.matcher(path);
      if (matcher.matches()) {
        groupPath = matcher.group(1);
        artifactId = matcher.group(2);
        version = matcher.group(3);
      }
    }

    if (groupPath != null) {
      if (HttpMethod.HEAD.matches(method)) {
        final String groupId = groupPath.replace('/', '.');
        return fileExists(request,
                          context,
                          groupPath,
                          groupId,
                          artifactId,
                          version,
                          fileName,
                          extension,
                          isChecksum,
                          isMetadata);
      } else if (HttpMethod.GET.matches(method)) {
        final String groupId = groupPath.replace('/', '.');
        return getFile(request,
                       context,
                       groupPath,
                       groupId,
                       artifactId,
                       version,
                       fileName,
                       extension,
                       isChecksum,
                       isMetadata);
      } else if (HttpMethod.PUT.matches(method)) {
        final String groupId = groupPath.replace('/', '.');
        return publishFile(request,
                           context,
                           groupPath,
                           groupId,
                           artifactId,
                           version,
                           fileName,
                           extension,
                           isChecksum,
                           isMetadata);
      }
    }

    return ResponseEntity.notFound()
      .build();
  }

  private ResponseEntity<StreamingResponseBody> unauthorized(final HttpServletRequest request) {
    // TODO: return better error message
    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.WWW_AUTHENTICATE,
                "Basic realm=\"Registree\"");
    return errorManager.getErrorResponse(HttpStatus.UNAUTHORIZED,
                                         headers,
                                         "Unauthorized");
  }

  private ResponseEntity<StreamingResponseBody> fileExists(final HttpServletRequest request,
                                                           final MavenOperationContext context,
                                                           final String grouPath,
                                                           final String groupId,
                                                           final String artifactId,
                                                           final String version,
                                                           final String fileName,
                                                           final String extension,
                                                           final boolean isChecksum,
                                                           final boolean isMetadata)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             grouPath,
                                                                             artifactId),
                                                                     Permission.READ);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    if (isChecksum && isMetadata) {
      final MavenChecksumManager checksumManager = context.getChecksumManager();
      return checksumManager.metadataChecksumExists(context,
                                                    request,
                                                    groupId,
                                                    artifactId,
                                                    fileName,
                                                    extension);
    } else if (isChecksum) {
      final MavenChecksumManager checksumManager = context.getChecksumManager();
      return checksumManager.artifactChecksumExists(context,
                                                    request,
                                                    groupId,
                                                    artifactId,
                                                    version,
                                                    fileName,
                                                    extension);
    } else if (isMetadata) {
      final MavenMetadataManager metadataManager = context.getMetadataManager();
      return metadataManager.metadataExists(context,
                                            request,
                                            groupId,
                                            artifactId);
    } else {
      final MavenArtifactManager artifactManager = context.getArtifactManager();
      return artifactManager.artifactExists(context,
                                            request,
                                            groupId,
                                            artifactId,
                                            version,
                                            fileName);
    }
  }

  private ResponseEntity<StreamingResponseBody> getFile(final HttpServletRequest request,
                                                        final MavenOperationContext context,
                                                        final String grouPath,
                                                        final String groupId,
                                                        final String artifactId,
                                                        final String version,
                                                        final String fileName,
                                                        final String extension,
                                                        final boolean isChecksum,
                                                        final boolean isMetadata)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             grouPath,
                                                                             artifactId),
                                                                     Permission.READ);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    if (isChecksum && isMetadata) {
      final MavenChecksumManager checksumManager = context.getChecksumManager();
      return checksumManager.getMetadataChecksum(context,
                                                 request,
                                                 groupId,
                                                 artifactId,
                                                 fileName,
                                                 extension);
    } else if (isChecksum) {
      final MavenChecksumManager checksumManager = context.getChecksumManager();
      return checksumManager.getArtifactChecksum(context,
                                                 request,
                                                 groupId,
                                                 artifactId,
                                                 version,
                                                 fileName,
                                                 extension);
    } else if (isMetadata) {
      final MavenMetadataManager metadataManager = context.getMetadataManager();
      return metadataManager.getMetadata(context,
                                         request,
                                         groupId,
                                         artifactId);
    } else {
      final MavenArtifactManager artifactManager = context.getArtifactManager();
      return artifactManager.getArtifact(context,
                                         request,
                                         groupId,
                                         artifactId,
                                         version,
                                         fileName);
    }
  }

  private ResponseEntity<StreamingResponseBody> publishFile(final HttpServletRequest request,
                                                            final MavenOperationContext context,
                                                            final String grouPath,
                                                            final String groupId,
                                                            final String artifactId,
                                                            final String version,
                                                            final String fileName,
                                                            final String extension,
                                                            final boolean isChecksum,
                                                            final boolean isMetadata)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             grouPath,
                                                                             artifactId),
                                                                     Permission.WRITE);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    if (isChecksum && isMetadata) {

      final MavenChecksumManager checksumManager = context.getChecksumManager();
      return checksumManager.publishMetadataChecksum(context,
                                                     request,
                                                     groupId,
                                                     artifactId,
                                                     fileName,
                                                     extension);
    } else if (isChecksum) {
      final MavenChecksumManager checksumManager = context.getChecksumManager();
      return checksumManager.publishArtifactChecksum(context,
                                                     request,
                                                     groupId,
                                                     artifactId,
                                                     version,
                                                     fileName,
                                                     extension);
    } else if (isMetadata) {
      final MavenMetadataManager metadataManager = context.getMetadataManager();
      return metadataManager.publishMetadata(context,
                                             request,
                                             groupId,
                                             artifactId);
    } else {
      final MavenArtifactManager artifactManager = context.getArtifactManager();
      return artifactManager.publishArtifact(context,
                                             request,
                                             groupId,
                                             artifactId,
                                             version,
                                             fileName);
    }
  }

  private String getPath(final MavenOperationContext context,
                         final String groupPath,
                         final String artifactId) {
    final String registryName = context.getRegistry().getName();
    return String.format("/%s/%s/%s", registryName, groupPath, artifactId);
  }

  private <T extends MavenManager> void addManagers(final Map<String, T> managersMap, final List<T> managers) {
    for (final T manager : managers) {
      managersMap.put(manager.getType().getValue(), manager);
    }
  }
}
