package com.core2web.service;

import java.io.File;

public class ImageUploadHelper {

    private ImageUploadHelper() {
    }

    public static CloudinaryService.UploadResult uploadRoom(File file) {
        return CloudinaryService.uploadImage(file, "roomImages");
    }

    public static CloudinaryService.UploadResult uploadProduct(File file) {
        return CloudinaryService.uploadImage(file, "productImages");
    }

    public static CloudinaryService.UploadResult uploadService(File file) {
        return CloudinaryService.uploadImage(file, "serviceImages");
    }

    public static CloudinaryService.UploadResult uploadProfile(File file) {
        return CloudinaryService.uploadImage(file, "profileImages");
    }
}