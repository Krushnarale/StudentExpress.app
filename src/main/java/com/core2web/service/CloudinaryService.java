package com.core2web.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.core2web.config.CloudinaryConfig;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class CloudinaryService {

    public static class UploadResult {

        private final String secureUrl;
        private final String publicId;

        public UploadResult(String secureUrl, String publicId) {
            this.secureUrl = secureUrl;
            this.publicId = publicId;
        }

        public String getSecureUrl() {
            return secureUrl;
        }

        public String getPublicId() {
            return publicId;
        }

        public boolean isSuccess() {
            return secureUrl != null && !secureUrl.isBlank();
        }
    }

    public static UploadResult uploadImage(File file, String folder) {
        return uploadImage(file, folder, UUID.randomUUID().toString(), false);
    }

    public static UploadResult uploadImage(File file, String folder, String customPublicId, boolean overwrite) {

        if (file == null || !file.exists()) {
            return new UploadResult("", "");
        }

        try {

            Cloudinary cloudinary = CloudinaryConfig.getCloudinary();

            Map<String, Object> result = cloudinary.uploader().upload(
                    file,
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", customPublicId != null ? customPublicId : UUID.randomUUID().toString(),
                            "resource_type", "image",
                            "overwrite", overwrite,
                            "transformation",
                            new Transformation()
                                    .width(1000)
                                    .height(1000)
                                    .crop("limit")
                                    .quality("auto")
                                    .fetchFormat("auto")));

            return new UploadResult(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id"));

        } catch (Exception e) {
            e.printStackTrace();
            return new UploadResult("", "");
        }
    }

    public static boolean deleteImage(String publicId) {

        if (publicId == null || publicId.isBlank()) {
            return false;
        }

        try {

            Cloudinary cloudinary = CloudinaryConfig.getCloudinary();

            Map<String, Object> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap());

            return "ok".equals(result.get("result"));

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}