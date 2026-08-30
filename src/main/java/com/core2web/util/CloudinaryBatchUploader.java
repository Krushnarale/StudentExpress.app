package com.core2web.util;

import com.core2web.service.CloudinaryService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CloudinaryBatchUploader {

    public static void uploadAllProjectImages() {
        System.out.println("   Starting Batch Cloudinary Upload for Assets   ");

        File assetsDir = ImageUtil.resolveFile("assets/image");
        if (assetsDir == null || !assetsDir.exists() || !assetsDir.isDirectory()) {
            System.err.println("[BatchUploader] Could not locate 'assets/image' directory!");
            return;
        }

        File[] files = assetsDir.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg");
        });

        if (files == null || files.length == 0) {
            System.out.println("[BatchUploader] No .jpg/.jpeg image files found in assets/image.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < files.length; i++) {
            File imgFile = files[i];
            String filename = imgFile.getName().toLowerCase();
            String baseName = filename.substring(0, filename.lastIndexOf('.'));
            String folder = "productImages";

            if (filename.startsWith("room_")) {
                folder = "roomImages";
            } else if (filename.startsWith("splash_") || filename.startsWith("welcome_")) {
                folder = "appAssets";
            }

            System.out.printf("[%d/%d] Uploading '%s' to folder '%s'...\n", (i + 1), files.length, imgFile.getName(), folder);

            CloudinaryService.UploadResult result = CloudinaryService.uploadImage(imgFile, folder, baseName, true);
            if (result != null && result.isSuccess()) {
                successCount++;
                System.out.println("   --> SUCCESS: " + result.getSecureUrl());
            } else {
                failCount++;
                System.err.println("   --> FAILED: " + imgFile.getName());
            }
        }

        System.out.printf("   Batch Upload Finished: %d Succeeded, %d Failed\n", successCount, failCount);
    }
}
