package com.itesoft.registree.oci.storage;

import static com.itesoft.registree.oci.storage.Constant.BLOB_FILE_PATH;
import static com.itesoft.registree.oci.storage.Constant.BLOB_PATH;
import static com.itesoft.registree.oci.storage.Constant.DATA_FILE_NAME;
import static com.itesoft.registree.oci.storage.Constant.TYPE_FILE_NAME;
import static com.itesoft.registree.oci.storage.Constant.UPLOAD_PATH;
import static com.itesoft.registree.oci.storage.OciDigestHelper.fromString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.oci.api.OciApiCallback;
import com.itesoft.registree.oci.dto.Blob;
import com.itesoft.registree.oci.dto.BlobUpload;
import com.itesoft.registree.registry.api.storage.StorageHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

@Service
public class BlobStorage {
  private static final Logger LOGGER = LoggerFactory.getLogger(BlobStorage.class);

  @Autowired
  private OciApiCallback ociApiCallback;

  @Autowired
  private StorageHelper storageHelper;

  private final Map<String, BlobUpload> uuidToUploadingBlob = new ConcurrentHashMap<>();

  @Scheduled(fixedDelay = 60000)
  public void cleanPendingBlobUploads() {
    final long now = System.currentTimeMillis();
    synchronized (uuidToUploadingBlob) {
      for (final String uuid : uuidToUploadingBlob.keySet()) {
        final BlobUpload blobUpload = uuidToUploadingBlob.get(uuid);
        if (blobUpload.getLastUpdate() < now - 60000) {
          LOGGER.warn("Removing pending upload of {} with uuid {} (expired at: {}, now: {})",
                      blobUpload.getName(),
                      blobUpload.getUuid(),
                      blobUpload.getLastUpdate(),
                      now);
          removeAndCloseBlobUpload(blobUpload);
          uuidToUploadingBlob.remove(uuid);
        }
      }
    }
  }

  public boolean blobUploadExists(final Registry registry,
                                  final String name,
                                  final String uuid) {
    synchronized (uuidToUploadingBlob) {
      final BlobUpload blobUpload = uuidToUploadingBlob.get(uuid);
      if (blobUpload == null || !name.endsWith(blobUpload.getName())) {
        return false;
      }
      return true;
    }
  }

  public BlobUpload getBlobUpload(final Registry registry,
                                  final String name,
                                  final String uuid) {
    synchronized (uuidToUploadingBlob) {
      final BlobUpload blobUpload = uuidToUploadingBlob.get(uuid);
      if (blobUpload == null || !name.endsWith(blobUpload.getName())) {
        return null;
      }
      blobUpload.setLastUpdate(System.currentTimeMillis());
      return blobUpload;
    }
  }

  public BlobUpload createBlobUpload(final Registry registry,
                                     final String name)
    throws IOException {
    final String uuid = UUID.randomUUID().toString();
    final Path target = getUploadFilePath(registry, name, uuid);
    Files.createDirectories(target.getParent());
    final File file = target.toFile();
    final OutputStream outputStream = new FileOutputStream(file);
    final BlobUpload blobUpload = new BlobUpload(name, uuid, outputStream);
    uuidToUploadingBlob.put(uuid, blobUpload);
    return blobUpload;
  }

  public void cancelBlobUpload(final Registry registry,
                               final String name,
                               final String uuid)
    throws IOException {
    removeAndCloseBlobUpload(uuid);
    FileSystemUtils.deleteRecursively(getUploadFileParent(registry, name, uuid));
  }

  public void createBlobFromUpload(final Registry registry,
                                   final String name,
                                   final String digest,
                                   final String uuid)
    throws IOException {
    removeAndCloseBlobUpload(uuid);
    final Path source = getUploadFilePath(registry, name, uuid);
    final Path target = getBlobDataFilePath(registry, digest);
    Files.createDirectories(target.getParent());
    Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); // TODO: is replace ok?
    FileSystemUtils.deleteRecursively(getUploadFileParent(registry, name, uuid));

    createOrUpdateFile(registry, digest, "application/vnd.docker.image.rootfs.diff.tar.gzip");
  }

  public Blob getBlob(final Registry registry,
                      final String digest)
    throws IOException {
    final Path blobDataPath = getBlobDataFilePath(registry, digest);
    if (!Files.isRegularFile(blobDataPath)) {
      return null;
    }
    final long contentLength = Files.size(blobDataPath);

    final Path blobTypePath = getBlobTypeFilePath(registry, digest);
    String contentType = null;
    if (Files.isRegularFile(blobTypePath)) {
      contentType = Files.readString(blobTypePath);
    }

    return new Blob(digest, contentType, contentLength, blobDataPath);
  }

  public byte[] getBlobData(final Registry registry,
                            final String digest)
    throws IOException {
    final Path blobPath = getBlobDataFilePath(registry, digest);
    if (!Files.isRegularFile(blobPath)) {
      return null;
    }
    return Files.readAllBytes(blobPath);
  }

  public void createBlob(final Registry registry,
                         final String digest,
                         final String type,
                         final byte[] data)
    throws IOException {
    final Path target = getBlobDataFilePath(registry, digest);
    Files.createDirectories(target.getParent());
    final File blobDataFile = target.toFile();
    blobDataFile.getParentFile().mkdirs();

    try (FileOutputStream fos = new FileOutputStream(blobDataFile)) {
      fos.write(data);
    }

    Files.write(getBlobTypeFilePath(registry, digest), type.getBytes());

    createOrUpdateFile(registry, digest, type);
  }

  public Path getBlobFilePath(final Registry registry,
                              final String digestAsString,
                              final String fileName) {
    final Digest digest = fromString(digestAsString);
    final String prefix = digest.getHex().substring(0, 2);
    final String relativePath = String.format(BLOB_PATH, digest.getAlgorithm(), prefix, digest.getHex(), fileName);
    return Paths.get(storageHelper.getStoragePath(registry),
                     relativePath);
  }

  private BlobUpload removeAndCloseBlobUpload(final String uuid) {
    final BlobUpload blobUpload = uuidToUploadingBlob.remove(uuid);
    return removeAndCloseBlobUpload(blobUpload);
  }

  private BlobUpload removeAndCloseBlobUpload(final BlobUpload blobUpload) {
    if (blobUpload != null) {
      final OutputStream outputStream = blobUpload.getOutputStream();
      try {
        outputStream.close();
      } catch (final IOException exception) {
        LOGGER.error(exception.getMessage(), exception);
      }
    }
    return blobUpload;
  }

  private void createOrUpdateFile(final Registry registry,
                                  final String digest,
                                  final String type) {
    final String filePath = String.format(BLOB_FILE_PATH, digest);
    if (!ociApiCallback.fileExists(registry, filePath)) {
      ociApiCallback.createFile(registry,
                                filePath,
                                type);
    } else {
      ociApiCallback.updateFile(registry,
                                filePath,
                                type);
    }
  }

  private Path getUploadFileParent(final Registry registry,
                                   final String name,
                                   final String uuid) {
    final String relativePath = String.format(UPLOAD_PATH, name, uuid);
    return Paths.get(storageHelper.getStoragePath(registry),
                     relativePath);
  }

  private Path getUploadFilePath(final Registry registry,
                                 final String name,
                                 final String uuid) {
    return Paths.get(getUploadFileParent(registry, name, uuid).toString(),
                     DATA_FILE_NAME);
  }

  private Path getBlobDataFilePath(final Registry registry,
                                   final String digestAsString) {
    return getBlobFilePath(registry, digestAsString, DATA_FILE_NAME);
  }

  private Path getBlobTypeFilePath(final Registry registry,
                                   final String digestAsString) {
    return getBlobFilePath(registry, digestAsString, TYPE_FILE_NAME);
  }
}
