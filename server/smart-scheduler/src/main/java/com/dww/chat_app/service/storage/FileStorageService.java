package com.dww.chat_app.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction for storing uploaded files independently from the backing storage implementation.
 */
public interface FileStorageService {

    /**
     * Stores an uploaded file and returns its opaque storage key.
     *
     * @param file uploaded file to store
     * @return generated storage key
     */
    String store(MultipartFile file);

    /**
     * Loads a stored file as a Spring resource.
     *
     * @param storageKey opaque key returned by {@link #store(MultipartFile)}
     * @return file resource
     */
    Resource loadAsResource(String storageKey);

    /**
     * Deletes a stored file when it exists.
     *
     * @param storageKey opaque key returned by {@link #store(MultipartFile)}
     */
    void delete(String storageKey);
}
