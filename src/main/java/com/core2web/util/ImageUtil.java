package com.core2web.util;

import javafx.scene.image.Image;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Base64;
import java.util.Iterator;

public class ImageUtil {

    public static Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        String str = imagePath.trim();

        // 1. Base64 check
        if (isBase64(str)) {
            Image img = base64ToImage(str);
            if (img != null && !img.isError()) {
                return img;
            }
        }

        try {
            // Direct URI/URL check (file:, http:, https:)
            if (str.startsWith("http://") || str.startsWith("https://") || str.startsWith("file:")) {
                Image img = new Image(str);
                if (!img.isError()) return img;
            }

            File file = resolveFile(str);
            if (file != null && file.exists()) {
                Image img = new Image(file.toURI().toString());
                if (!img.isError()) return img;
            }

            // Check classpath resource
            String resourcePath = str.startsWith("/") ? str : "/" + str;
            InputStream is = ImageUtil.class.getResourceAsStream(resourcePath);
            if (is != null) {
                Image img = new Image(is);
                if (!img.isError()) return img;
            }

            // Cloudinary CDN Fallback for packaged App / EXE / deleted local files
            try {
                String cleanName = str;
                if (cleanName.contains("/") || cleanName.contains("\\")) {
                    cleanName = cleanName.substring(Math.max(cleanName.lastIndexOf('/'), cleanName.lastIndexOf('\\')) + 1);
                }
                String ext = str.toLowerCase().endsWith(".png") ? ".png" : ".jpg";
                if (cleanName.contains(".")) {
                    cleanName = cleanName.substring(0, cleanName.lastIndexOf('.'));
                }
                String folder = cleanName.startsWith("room_") ? "roomImages" : (cleanName.startsWith("splash_") || cleanName.startsWith("welcome_") ? "appAssets" : "productImages");

                String cdnUrl = "https://res.cloudinary.com/dm9hshdz/image/upload/" + folder + "/" + cleanName + ext;
                Image img = new Image(cdnUrl);
                if (!img.isError()) return img;

                String altExt = ext.equals(".jpg") ? ".png" : ".jpg";
                String altCdnUrl = "https://res.cloudinary.com/dm9hshdz/image/upload/" + folder + "/" + cleanName + altExt;
                Image altImg = new Image(altCdnUrl);
                if (!altImg.isError()) return altImg;
            } catch (Exception ignored) {}
        } catch (Exception e) {
            System.err.println("[ImageUtil] Could not load image from " + imagePath + ": " + e.getMessage());
        }
        return getFallbackImage();
    }

    public static String compressAndEncode(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return "";
        }

        try {
            BufferedImage originalImage = ImageIO.read(file);
            if (originalImage == null) return "";

            int maxDim = 800;
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            if (width > maxDim || height > maxDim) {
                double scale = Math.min((double) maxDim / width, (double) maxDim / height);
                width = (int) Math.max(1, width * scale);
                height = (int) Math.max(1, height * scale);
            }

            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, width, height, null);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (writers.hasNext()) {
                ImageWriter writer = writers.next();
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(0.70f); // 70% quality
                }
                try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                    writer.setOutput(ios);
                    writer.write(null, new IIOImage(resizedImage, null, null), param);
                }
                writer.dispose();
            } else {
                ImageIO.write(resizedImage, "jpg", baos);
            }

            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Throwable e) {
            System.err.println("[ImageUtil] Compression failed: " + e.getMessage());
            return "";
        }
    }

    public static String imageToBase64(File file) {
        return compressAndEncode(file);
    }

    public static Image base64ToImage(String base64Str) {
        if (base64Str == null || base64Str.trim().isEmpty()) {
            return null;
        }

        try {
            String cleanStr = base64Str.trim();
            if (cleanStr.contains(",")) {
                cleanStr = cleanStr.substring(cleanStr.indexOf(",") + 1);
            }
            cleanStr = cleanStr.replaceAll("\\s+", "");
            byte[] bytes = Base64.getDecoder().decode(cleanStr);
            Image img = new Image(new ByteArrayInputStream(bytes));
            if (img != null && !img.isError()) {
                return img;
            }
        } catch (Throwable e) {
            System.err.println("[ImageUtil] Error decoding Base64 image: " + e.getMessage());
        }
        return null;
    }

    private static boolean isBase64(String str) {
        if (str == null || str.length() < 50) return false;
        if (str.startsWith("data:image")) return true;
        if (str.startsWith("assets/") || str.startsWith("assets\\")
                || str.startsWith("http://") || str.startsWith("https://") || str.startsWith("file:")) {
            return false;
        }
        if (str.contains("\\")) return false;

        String clean = str.trim();
        if (clean.contains(",")) {
            clean = clean.substring(clean.indexOf(",") + 1);
        }
        clean = clean.replaceAll("\\s+", "");
        try {
            byte[] bytes = Base64.getDecoder().decode(clean.length() > 200 ? clean.substring(0, 100) : clean);
            return bytes != null && bytes.length > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static File resolveFile(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        // Check subproject path first if running from parent directory
        File fSub = new File("studentx/studentexpress", imagePath);
        if (fSub.exists()) return fSub;

        File fSub2 = new File("studentexpress", imagePath);
        if (fSub2.exists()) return fSub2;

        File f1 = new File(imagePath);
        if (f1.exists()) return f1;

        if (!imagePath.contains("assets/image") && !imagePath.contains("assets\\image")) {
            File fAssets = resolveFile("assets/image/" + imagePath);
            if (fAssets != null && fAssets.exists()) return fAssets;
        }

        File currentDir = new File(System.getProperty("user.dir", "."));
        for (int i = 0; i < 3 && currentDir != null; i++) {
            File candidateSub = new File(new File(currentDir, "studentx/studentexpress"), imagePath);
            if (candidateSub.exists()) return candidateSub;

            File candidate = new File(currentDir, imagePath);
            if (candidate.exists()) return candidate;

            currentDir = currentDir.getParentFile();
        }

        if (imagePath.endsWith(".png")) {
            String jpgPath = imagePath.substring(0, imagePath.length() - 4) + ".jpg";
            File jpgFile = resolveFile(jpgPath);
            if (jpgFile != null && jpgFile.exists()) return jpgFile;
        } else if (imagePath.endsWith(".jpg")) {
            String pngPath = imagePath.substring(0, imagePath.length() - 4) + ".png";
            File pngFile = resolveFile(pngPath);
            if (pngFile != null && pngFile.exists()) return pngFile;
        }

        return f1;
    }

    private static Image getFallbackImage() {
        try {
            File fallback = resolveFile("assets/image/room_single.png");
            if (fallback != null && fallback.exists()) {
                Image img = new Image(fallback.toURI().toString());
                if (!img.isError()) return img;
            }
        } catch (Exception e) {}
        return null;
    }
}

