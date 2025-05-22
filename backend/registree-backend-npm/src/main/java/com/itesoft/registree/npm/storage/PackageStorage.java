package com.itesoft.registree.npm.storage;

import static com.itesoft.registree.IoHelper.closeSilently;
import static com.itesoft.registree.IoHelper.deleteSilently;
import static com.itesoft.registree.java.DigestHelper.bytesToHex;
import static com.itesoft.registree.npm.dto.PackageExistenceState.EXISTS;
import static com.itesoft.registree.npm.dto.PackageExistenceState.MATCHES;
import static com.itesoft.registree.npm.dto.PackageExistenceState.UNKNOWN;
import static com.itesoft.registree.npm.storage.NpmHelper.extractVersionFromFileName;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.itesoft.registree.RandomHelper;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.npm.api.NpmApiCallback;
import com.itesoft.registree.npm.dto.GetPackageResult;
import com.itesoft.registree.npm.dto.PackageExistenceState;
import com.itesoft.registree.npm.dto.TarballCreation;
import com.itesoft.registree.npm.dto.json.Attachment;
import com.itesoft.registree.npm.dto.json.RequestPackage;
import com.itesoft.registree.npm.dto.json.ResponsePackage;
import com.itesoft.registree.npm.dto.json.Version;
import com.itesoft.registree.npm.dto.json.VersionDist;
import com.itesoft.registree.registry.api.storage.StorageHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

// TODO: add some write lock on tarball
@Component
public class PackageStorage {
  public static final String PACKAGE_JSON_FILENAME = "package.json";
  private static final String REGISTRY_URI_PATTERN = "@@@registryUri@@@";

  @Autowired
  private StorageHelper storageHelper;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ConversionService conversionService;

  @Autowired
  private NpmApiCallback apiCallback;

  // TODO: add some cleanup to avoid memory leak
  private final Map<String, ReentrantReadWriteLock> rwLockPerPath = new HashMap<>();

  public PackageExistenceState packageExists(final Registry registry,
                                             final String packageScope,
                                             final String packageName,
                                             final String knownChecksum)
    throws Exception {
    final Path packageJsonPath = getPackageJsonPath(registry, packageScope, packageName);
    if (!Files.isRegularFile(packageJsonPath)) {
      return UNKNOWN;
    }

    final Lock lock = readLock(packageJsonPath);
    try {
      if (knownChecksum != null) {
        final String currentChecksum = getPackageJsonChecksum(packageJsonPath);
        if (knownChecksum.equals(currentChecksum)) {
          return MATCHES;
        }
      }

      return EXISTS;
    } finally {
      unlock(lock);
    }
  }

  public String getPackageChecksum(final Registry registry,
                                   final String packageScope,
                                   final String packageName)
    throws IOException {
    final Path packageJsonPath = getPackageJsonPath(registry, packageScope, packageName);
    if (!Files.isRegularFile(packageJsonPath)) {
      return null;
    }

    final Lock lock = readLock(packageJsonPath);
    try {
      return getPackageJsonChecksum(packageJsonPath);
    } finally {
      unlock(lock);
    }
  }

  public GetPackageResult getPackage(final Registry registry,
                                     final String registryUri,
                                     final String packageScope,
                                     final String packageName)
    throws Exception {
    final Path packageJsonPath = getPackageJsonPath(registry, packageScope, packageName);
    if (!Files.isRegularFile(packageJsonPath)) {
      return null;
    }

    final Lock lock = readLock(packageJsonPath);
    try {
      final ResponsePackage responsePackage =
        objectMapper.readValue(packageJsonPath.toFile(),
                               ResponsePackage.class);

      fillTarballRegistryUri(registryUri, responsePackage.getVersions().values());

      final String checksum = getPackageJsonChecksum(packageJsonPath);

      return GetPackageResult.builder()
        .responsePackage(responsePackage)
        .checksum(checksum)
        .build();
    } finally {
      unlock(lock);
    }
  }

  public Version getPackageVersion(final Registry registry,
                                   final String registryUri,
                                   final String packageScope,
                                   final String packageName,
                                   final String packageVersion)
    throws Exception {
    final Path packageJsonPath = getPackageJsonPath(registry, packageScope, packageName);
    if (!Files.isRegularFile(packageJsonPath)) {
      return null;
    }

    final Lock lock = readLock(packageJsonPath);
    try {
      final ResponsePackage responsePackage =
        objectMapper.readValue(packageJsonPath.toFile(),
                               ResponsePackage.class);

      final Map<String, Version> versions = responsePackage.getVersions();
      if (versions == null) {
        return null;
      }
      final Version version = versions.get(packageVersion);
      if (version == null) {
        return null;
      }

      fillTarballRegistryUri(registryUri, Arrays.asList(version));

      return version;
    } finally {
      unlock(lock);
    }
  }

  public Path getTarballFilePath(final Registry registry,
                                 final String packageScope,
                                 final String packageName,
                                 final String fileName) {
    final Path tarballPath = getTarballPath(registry,
                                            packageScope,
                                            packageName,
                                            fileName);
    if (!Files.isRegularFile(tarballPath)) {
      return null;
    }
    return tarballPath;
  }

  public String publishPackage(final Registry registry,
                               final String packageScope,
                               final String packageName,
                               final RequestPackage requestPackage)
    throws Exception {
    // TODO: prevent multiple versions usecase
    final String packageVersion = requestPackage.getVersions().keySet().iterator().next();

    final Path packageJsonPath = getPackageJsonPath(registry, packageScope, packageName);

    final Lock lock = writeLock(packageJsonPath);
    try {
      final ResponsePackage newPackage;
      if (Files.isRegularFile(packageJsonPath)) {
        newPackage = objectMapper.readValue(packageJsonPath.toFile(),
                                            ResponsePackage.class);

        fixVersions(getPackageScopeAndNamePath(packageScope, packageName),
                    requestPackage.getVersions().values());

        newPackage.getVersions().putAll(requestPackage.getVersions());

        mergeDistTags(newPackage.getDistTags(),
                      requestPackage.getDistTags());
      } else {
        Files.createDirectories(packageJsonPath.getParent());

        newPackage = conversionService.convert(requestPackage, ResponsePackage.class);
        newPackage.setTime(new LinkedHashMap<>());

        fixVersions(getPackageScopeAndNamePath(packageScope, packageName),
                    requestPackage.getVersions().values());
      }
      requestPackage.getVersions()
        .keySet()
        .forEach((v) -> newPackage.getTime().put(v, OffsetDateTime.now()));

      final byte[] digest = writeResponsePackage(packageJsonPath, newPackage);
      final Path checksumPath = getChecksumPath(packageJsonPath);
      final String checksum = bytesToHex(digest);

      // TODO: prevent multiple attachments usecase?
      final Attachment attachment = requestPackage.getAttachments().values().iterator().next();
      final Path tempTgzPath = getTarballPathWithVersion(registry,
                                                         packageScope,
                                                         packageName,
                                                         packageVersion,
                                                         true);

      try (OutputStream outputStream = Files.newOutputStream(tempTgzPath)) {
        final Decoder decoder = Base64.getDecoder();
        try (InputStream base64InputStream = new ByteArrayInputStream(attachment.getData().getBytes());
            InputStream decodedStream = decoder.wrap(base64InputStream)) {
          final byte[] buffer = new byte[8192];
          int bytesRead;
          while ((bytesRead = decodedStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
          }
        }
      }

      final Path tgzPath = getTarballPathWithVersion(registry,
                                                     packageScope,
                                                     packageName,
                                                     packageVersion);

      createOrUpdateComponentAndFile(registry,
                                     packageScope,
                                     packageName,
                                     packageVersion);

      Files.write(checksumPath, checksum.getBytes());
      Files.move(tempTgzPath,
                 tgzPath,
                 StandardCopyOption.ATOMIC_MOVE,
                 StandardCopyOption.REPLACE_EXISTING);

      return checksum;
    } finally {
      unlock(lock);
    }
  }

  public GetPackageResult createPackageJson(final Registry registry,
                                            final String registryUri,
                                            final String packageScope,
                                            final String packageName,
                                            final ResponsePackage responsePackage,
                                            final String checksum)
    throws Exception {
    fixVersions(getPackageScopeAndNamePath(packageScope, packageName),
                responsePackage.getVersions().values());

    final Path packageJsonPath = getPackageJsonPath(registry, packageScope, packageName);

    final Lock lock = writeLock(packageJsonPath);
    try {
      Files.createDirectories(packageJsonPath.getParent());

      try (OutputStream outputStream = Files.newOutputStream(packageJsonPath)) {
        objectMapper.writeValue(outputStream,
                                responsePackage);
      }

      if (checksum != null) {
        final Path checksumPath = getChecksumPath(packageJsonPath);
        Files.write(checksumPath, checksum.getBytes());
      }

      fillTarballRegistryUri(registryUri, responsePackage.getVersions().values());

      return GetPackageResult.builder()
        .responsePackage(responsePackage)
        .checksum(checksum)
        .build();
    } finally {
      unlock(lock);
    }
  }

  public void fixResponsePackage(final String registryUri,
                                 final String packageScope,
                                 final String packageName,
                                 final ResponsePackage responsePackage) {
    fixVersions(registryUri,
                getPackageScopeAndNamePath(packageScope, packageName),
                responsePackage.getVersions().values());
  }

  public void mergeDistTags(final Map<String, String> currentDistTags,
                            final Map<String, String> distTagsToMerge) {
    final String latestVersion = getActualLatestVersion(currentDistTags,
                                                        distTagsToMerge);
    currentDistTags.putAll(distTagsToMerge);
    currentDistTags.put("latest", latestVersion);
  }

  public void prepareTarballCreation(final Registry registry,
                                     final String packageScope,
                                     final String packageName,
                                     final String fileName) {
    final String packageVersion = extractVersionFromFileName(packageName, fileName);
    createOrUpdateComponentAndFile(registry,
                                   packageScope,
                                   packageName,
                                   packageVersion);
  }

  public TarballCreation initiateTarballCreation(final Registry registry,
                                                 final String packageScope,
                                                 final String packageName,
                                                 final String fileName)
    throws IOException {
    final Path tarballPath = getTarballPath(registry, packageScope, packageName, fileName);
    Files.createDirectories(tarballPath.getParent());

    final Path tempTarballPath = getTarballPath(registry, packageScope, packageName, fileName, true);
    final OutputStream outputStream = Files.newOutputStream(tempTarballPath);

    return TarballCreation.builder()
      .tarballPath(tarballPath)
      .tempTarballPath(tempTarballPath)
      .outputStream(outputStream)
      .build();
  }

  public void createTarball(final Registry registry,
                            final TarballCreation tarballCreation)
    throws IOException {
    closeSilently(tarballCreation.getOutputStream());

    Files.move(tarballCreation.getTempTarballPath(),
               tarballCreation.getTarballPath(),
               StandardCopyOption.ATOMIC_MOVE,
               StandardCopyOption.REPLACE_EXISTING);
  }

  public void abortTarballCreation(final Registry registry,
                                   final TarballCreation tarballCreation) {
    closeSilently(tarballCreation.getOutputStream());
    deleteSilently(tarballCreation.getTempTarballPath());
  }

  public void deleteTarball(final Registry registry,
                            final String packageScope,
                            final String packageName,
                            final String packageVersion)
    throws Exception {
    final Path tarballPath =
      getTarballPathWithVersion(registry, packageScope, packageName, packageVersion);
    Files.deleteIfExists(tarballPath);

    final Path packageJsonPath = getPackageJsonPath(registry, packageScope, packageName);
    if (!Files.isRegularFile(packageJsonPath)) {
      return;
    }

    final Lock lock = writeLock(packageJsonPath);
    try {
      final ResponsePackage responsePackage =
        objectMapper.readValue(packageJsonPath.toFile(),
                               ResponsePackage.class);

      responsePackage.getVersions().remove(packageVersion);
      boolean doRemove = false;
      final String latestVersion = responsePackage.getDistTags().get("latest");
      if (latestVersion.equals(packageVersion)) {
        final String latest = getActualLatestVersion(responsePackage.getVersions().keySet());
        if (latest == null) {
          doRemove = true;
        } else {
          responsePackage.getDistTags().put("latest", latestVersion);
        }
      }

      if (doRemove) {
        Files.delete(packageJsonPath);
      } else {
        responsePackage.getTime().remove(packageVersion);
        responsePackage.getDistTags().remove(packageVersion);

        final byte[] digest = writeResponsePackage(packageJsonPath, responsePackage);
        final Path checksumPath = getChecksumPath(packageJsonPath);
        final String checksum = bytesToHex(digest);

        Files.write(checksumPath, checksum.getBytes());
      }
    } finally {
      unlock(lock);
    }
  }

  private void fixVersions(final String name,
                           final Collection<Version> versions) {
    fixVersions(REGISTRY_URI_PATTERN,
                name,
                versions);
  }

  private void fixVersions(final String registryUri,
                           final String name,
                           final Collection<Version> versions) {
    for (final Version version : versions) {
      final VersionDist dist = version.getDist();
      final String tarballUri = dist.getTarball();
      final int index = tarballUri.lastIndexOf('/');
      final String fileName = tarballUri.substring(index + 1);
      dist.setTarball(String.format("%s/%s/-/%s",
                                    registryUri,
                                    name,
                                    fileName));
      version.setResolved(String.format("file:dist/%s", name));
      version.setFrom(".");
    }
  }

  private void fillTarballRegistryUri(final String registryUri,
                                      final Collection<Version> versions) {
    for (final Version version : versions) {
      final VersionDist dist = version.getDist();
      String tarballUri = dist.getTarball();
      tarballUri = tarballUri.replace(REGISTRY_URI_PATTERN, registryUri);
      dist.setTarball(tarballUri);
    }
  }

  private String getPackageJsonChecksum(final Path packageJsonPath) throws IOException {
    final Path checksumPath = getChecksumPath(packageJsonPath);
    if (!Files.isRegularFile(checksumPath)) {
      return null;
    }
    return Files.readString(checksumPath);
  }

  private String getActualLatestVersion(final Map<String, String> existingDistTags,
                                        final Map<String, String> newDistTags) {
    final String latestVersion = existingDistTags.get("latest");
    com.github.zafarkhaja.semver.Version actualLatestVersion =
      com.github.zafarkhaja.semver.Version.parse(latestVersion);
    for (final String v : newDistTags.values()) {
      final com.github.zafarkhaja.semver.Version versionToCompare =
        com.github.zafarkhaja.semver.Version.parse(v);
      if (versionToCompare.isHigherThan(actualLatestVersion)) {
        actualLatestVersion = versionToCompare;
      }
    }
    return actualLatestVersion.toString();
  }

  private String getActualLatestVersion(final Collection<String> versions) {
    if (versions == null || versions.isEmpty()) {
      return null;
    }
    final String latestVersion = versions.iterator().next();
    return getActualLatestVersion(latestVersion, versions);
  }

  private String getActualLatestVersion(final String latestVersion,
                                        final Collection<String> versions) {
    com.github.zafarkhaja.semver.Version actualLatestVersion =
      com.github.zafarkhaja.semver.Version.parse(latestVersion);
    for (final String v : versions) {
      final com.github.zafarkhaja.semver.Version versionToCompare =
        com.github.zafarkhaja.semver.Version.parse(v);
      if (versionToCompare.isHigherThan(actualLatestVersion)) {
        actualLatestVersion = versionToCompare;
      }
    }
    return actualLatestVersion.toString();
  }

  private byte[] writeResponsePackage(final Path packageJsonPath,
                                      final ResponsePackage responsePackage)
    throws Exception {
    final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    try (OutputStream outputStream = Files.newOutputStream(packageJsonPath);
        DigestOutputStream digestOutputStream = new DigestOutputStream(outputStream, messageDigest)) {
      objectMapper.writeValue(digestOutputStream,
                              responsePackage);
    }

    return messageDigest.digest();
  }

  private void createOrUpdateComponentAndFile(final Registry registry,
                                              final String packageScope,
                                              final String packageName,
                                              final String packageVersion) {
    final String tarballPath = String.format("%s/%s-%s.tgz",
                                             getPackageScopeAndNamePath(packageScope,
                                                                        packageName),
                                             packageName,
                                             packageVersion);

    boolean exists = apiCallback.componentExists(registry,
                                                 packageScope,
                                                 packageName,
                                                 packageVersion);
    final String componentId;
    if (exists) {
      componentId =
        apiCallback.updateComponent(registry,
                                    packageScope,
                                    packageName,
                                    packageVersion);
    } else {
      componentId =
        apiCallback.createComponent(registry,
                                    packageScope,
                                    packageName,
                                    packageVersion);
    }

    exists = apiCallback.fileExists(registry,
                                    tarballPath);
    if (exists) {
      apiCallback.updateFile(registry,
                             componentId,
                             tarballPath,
                             MediaType.APPLICATION_OCTET_STREAM_VALUE);
    } else {
      apiCallback.createFile(registry,
                             componentId,
                             tarballPath,
                             MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }
  }

  private Lock readLock(final Path path) {
    final ReentrantReadWriteLock reentrantReadWriteLock = getReadWriteLock(path);
    final ReadLock readLock = reentrantReadWriteLock.readLock();
    readLock.lock();
    return readLock;
  }

  private Lock writeLock(final Path path) {
    final ReentrantReadWriteLock reentrantReadWriteLock = getReadWriteLock(path);
    final WriteLock writeLock = reentrantReadWriteLock.writeLock();
    writeLock.lock();
    return writeLock;
  }

  private void unlock(final Lock lock) {
    lock.unlock();
  }

  private ReentrantReadWriteLock getReadWriteLock(final Path path) {
    final String pathAsString = path.toString();
    ReentrantReadWriteLock reentrantReadWriteLock;
    synchronized (rwLockPerPath) {
      reentrantReadWriteLock = rwLockPerPath.get(pathAsString);
      reentrantReadWriteLock = new ReentrantReadWriteLock();
      rwLockPerPath.put(pathAsString, reentrantReadWriteLock);
    }
    return reentrantReadWriteLock;
  }

  private Path getPackageJsonPath(final Registry registry,
                                  final String packageScope,
                                  final String packageName) {
    return Paths.get(storageHelper.getStoragePath(registry),
                     getPackageScopeAndNamePath(packageScope,
                                                packageName),
                     PACKAGE_JSON_FILENAME);
  }

  private Path getTarballPathWithVersion(final Registry registry,
                                         final String packageScope,
                                         final String packageName,
                                         final String packageVersion) {
    return getTarballPathWithVersion(registry, packageScope, packageName, packageVersion, false);
  }

  private Path getTarballPathWithVersion(final Registry registry,
                                         final String packageScope,
                                         final String packageName,
                                         final String packageVersion,
                                         final boolean temp) {
    final String fileName = String.format("%s-%s.tgz",
                                          packageName,
                                          packageVersion)
      + (temp ? "." + RandomHelper.random(6) : "");
    return Paths.get(storageHelper.getStoragePath(registry),
                     getPackageScopeAndNamePath(packageScope,
                                                packageName),
                     fileName);
  }

  private Path getTarballPath(final Registry registry,
                              final String packageScope,
                              final String packageName,
                              final String fileName) {
    return getTarballPath(registry, packageScope, packageName, fileName, false);
  }

  private Path getTarballPath(final Registry registry,
                              final String packageScope,
                              final String packageName,
                              final String fileName,
                              final boolean temp) {
    final String actualFileName = fileName + (temp ? "." + RandomHelper.random(6) : "");
    return Paths.get(storageHelper.getStoragePath(registry),
                     getPackageScopeAndNamePath(packageScope,
                                                packageName),
                     actualFileName);
  }

  private Path getChecksumPath(final Path packageJsonPath) {
    final String packageJsonPathAsString = packageJsonPath.toString();
    return Paths.get(packageJsonPathAsString + ".checksum");
  }

  private String getPackageScopeAndNamePath(final String packageScope, final String packageName) {
    if (packageScope == null) {
      return packageName;
    } else {
      return Paths.get(packageScope, packageName).toString();
    }
  }
}
