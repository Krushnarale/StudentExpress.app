package com.core2web.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class CloudinaryConfig {

    private static Cloudinary cloudinary;

    private CloudinaryConfig() {
    }

    public static synchronized Cloudinary getCloudinary() {

        if (cloudinary == null) {

            cloudinary = new Cloudinary(
                    ObjectUtils.asMap(
                            "cloud_name", "dm9hshdz",
                            "api_key", "562352383324696",
                            "api_secret", "OrubSvY6b66PYy9nV_kkFbmSGJo",
                            "secure", true));
        }

        return cloudinary;
    }
}