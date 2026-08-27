package com.core2web.util;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FirebaseStorageUtil {

    private static Storage storage;

    public static Storage getStorageInstance() {
        if (storage == null) {
            try {
                storage = StorageOptions.getDefaultInstance().getService();
            } catch (Exception e) {
                System.err.println("[FirebaseStorageUtil] Failed to initialize Storage service: " + e.getMessage());
            }
        }
        return storage;
    }

    public static String uploadFile(String bucketName, String objectName, String filePath) {
        try {
            Storage storageService = getStorageInstance();
            if (storageService == null) return null;

            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            byte[] content = Files.readAllBytes(Paths.get(filePath));
            storageService.create(blobInfo, content);
            return "https://storage.googleapis.com/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            System.err.println("[FirebaseStorageUtil] File upload failed: " + e.getMessage());
            return null;
        }
    }
}
