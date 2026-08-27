package com.core2web.util;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageConverter {
    public static void convertAllJpgToPng() {
        File dir = new File("assets/image");
        if (!dir.exists()) return;
        
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jpg"));
        if (files == null) return;

        for (File jpgFile : files) {
            try {
                BufferedImage img = ImageIO.read(jpgFile);
                if (img != null) {
                    String baseName = jpgFile.getName().substring(0, jpgFile.getName().lastIndexOf('.'));
                    File pngFile = new File(dir, baseName + ".png");
                    ImageIO.write(img, "png", pngFile);
                    System.out.println("Converted " + jpgFile.getName() + " -> " + pngFile.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Also copy room images to room_thumb1, room_thumb2, room_thumb3, room_thumb4 for RoomDetailsPage gallery
        try {
            File rSingle = new File(dir, "room_single.png");
            File rFlat = new File(dir, "room_flat.png");
            File rPg = new File(dir, "room_pg.png");
            File rSharing = new File(dir, "room_sharing.png");

            if (rSingle.exists()) ImageIO.write(ImageIO.read(rSingle), "png", new File(dir, "room_thumb1.png"));
            if (rFlat.exists()) ImageIO.write(ImageIO.read(rFlat), "png", new File(dir, "room_thumb2.png"));
            if (rPg.exists()) ImageIO.write(ImageIO.read(rPg), "png", new File(dir, "room_thumb3.png"));
            if (rSharing.exists()) ImageIO.write(ImageIO.read(rSharing), "png", new File(dir, "room_thumb4.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        convertAllJpgToPng();
    }
}
