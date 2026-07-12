package com.dww.chat_app.service.storage;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocalFileStorageService implements FileStorageService {

    static final int STORAGE_KEY_BYTES = 32;
    static final int MAX_KEY_GENERATION_ATTEMPTS = 5;
    static final Pattern STORAGE_KEY_PATTERN = Pattern.compile("[A-Za-z0-9_-]{43}");
    static final String TEMPORARY_FILE_PREFIX = ".upload-";
    static final String TEMPORARY_FILE_SUFFIX = ".tmp";

    Path storageRoot;
    SecureRandom secureRandom = new SecureRandom();

    public LocalFileStorageService(@Value("${storage.local.root-directory:./uploads}") String storageRoot) {
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initializeStorageRoot() {
        try {
            Files.createDirectories(storageRoot);

            if (!Files.isDirectory(storageRoot) || !Files.isWritable(storageRoot)) {
                throw new IllegalStateException("Configured local storage directory is not writable");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize local file storage", exception);
        }
    }

    @Override
    public String store(MultipartFile file) {
        validateFile(file);

        Path temporaryFile = null;
        Path destination = null;
        boolean stored = false;

        try {
            temporaryFile = Files.createTempFile(storageRoot, TEMPORARY_FILE_PREFIX, TEMPORARY_FILE_SUFFIX);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
            }

            StorageTarget storageTarget = reserveStorageTarget();
            destination = storageTarget.path();
            moveTemporaryFile(temporaryFile, destination);

            stored = true;
            return storageTarget.storageKey();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store uploaded file", exception);
        } finally {
            if (!stored) {
                deleteQuietly(temporaryFile);
                deleteQuietly(destination);
            }
        }
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        Path path = resolveStoragePath(storageKey);

        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("Stored file does not exist");
        }

        return new FileSystemResource(path);
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolveStoragePath(storageKey));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to delete stored file", exception);
        }
    }

    private StorageTarget reserveStorageTarget() {
        for (int attempt = 0; attempt < MAX_KEY_GENERATION_ATTEMPTS; attempt++) {
            String storageKey = generateStorageKey();
            Path path = resolveStoragePath(storageKey);

            try {
                Files.createFile(path);
                return new StorageTarget(storageKey, path);
            } catch (FileAlreadyExistsException ignored) {
                // A collision is exceptionally unlikely; generate a fresh opaque key and retry.
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to reserve local storage target", exception);
            }
        }

        throw new IllegalStateException("Unable to generate a unique storage key");
    }

    private String generateStorageKey() {
        byte[] randomBytes = new byte[STORAGE_KEY_BYTES];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private Path resolveStoragePath(String storageKey) {
        if (storageKey == null || !STORAGE_KEY_PATTERN.matcher(storageKey).matches()) {
            throw new IllegalArgumentException("Invalid storage key");
        }

        Path resolvedPath = storageRoot.resolve(storageKey).normalize();
        if (!resolvedPath.startsWith(storageRoot) || !storageRoot.equals(resolvedPath.getParent())) {
            throw new IllegalArgumentException("Invalid storage key");
        }

        return resolvedPath;
    }

    private void moveTemporaryFile(Path temporaryFile, Path destination) throws IOException {
        try {
            Files.move(
                    temporaryFile,
                    destination,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }

        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Preserve the primary storage failure while making a best-effort cleanup.
        }
    }

    private record StorageTarget(String storageKey, Path path) {
    }
}
