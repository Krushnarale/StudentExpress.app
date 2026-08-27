package com.core2web.util;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageGenerator {

    public static void generateAllImages() {
        File dir = new File("assets/image");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        generateImage("book_math.png", "Engineering Mathematics", "M1 - M2 Textbook", new Color(245, 243, 238), new Color(46, 117, 89), "BOOK");
        generateImage("laptop_dell.png", "Dell Laptop i5", "8GB RAM - 256GB SSD", new Color(240, 242, 245), new Color(50, 60, 70), "LAPTOP");
        generateImage("table_study.png", "Study Table & Lamp", "Solid Wood Desk", new Color(248, 244, 236), new Color(139, 90, 43), "TABLE");
        generateImage("cycle_hero.png", "Hero Sprint Cycle", "21-Speed Gear Bicycle", new Color(242, 245, 240), new Color(34, 139, 34), "CYCLE");
        generateImage("airpods_boat.png", "Boat Airdopes 141", "Wireless Earbuds", new Color(245, 245, 247), new Color(75, 85, 99), "AIRPODS");
        generateImage("backpack_skybags.png", "Skybags Backpack", "Laptop Backpack", new Color(240, 244, 248), new Color(30, 64, 175), "BACKPACK");
        generateImage("iphone_11.png", "iPhone 11 (64GB)", "Apple Smartphone", new Color(242, 244, 248), new Color(71, 85, 105), "PHONE");
        generateImage("chair_office.png", "Ergonomic Office Chair", "Mesh Back Executive", new Color(245, 245, 245), new Color(31, 41, 55), "CHAIR");

        // Rooms
        generateRoomImage("room_single.png", "Single Furnished Room", "Kothrud, Pune - ₹ 6,000/mo", new Color(138, 171, 143), new Color(245, 242, 235));
        generateRoomImage("room_pg.png", "PG for Boys", "Hinjewadi, Pune - ₹ 7,500/mo", new Color(160, 185, 168), new Color(248, 246, 240));
        generateRoomImage("room_sharing.png", "2 Sharing Room", "Baner, Pune - ₹ 4,500/mo", new Color(145, 175, 180), new Color(242, 245, 248));
        generateRoomImage("room_studio.png", "Studio Apartment", "Viman Nagar - ₹ 11,000/mo", new Color(175, 160, 145), new Color(248, 244, 240));

        // Gallery Thumbnails
        generateRoomImage("room_thumb1.png", "Bedroom View", "Spacious King Bed", new Color(140, 170, 145), new Color(245, 242, 235));
        generateRoomImage("room_thumb2.png", "Study Desk", "Ergonomic Setup", new Color(150, 165, 140), new Color(245, 245, 240));
        generateRoomImage("room_thumb3.png", "Kitchen Area", "Modular Kitchen", new Color(160, 175, 160), new Color(248, 248, 245));
        generateRoomImage("room_thumb4.png", "Attached Bathroom", "Clean & Sanitized", new Color(145, 160, 175), new Color(240, 245, 248));

        // Welcome / Splash
        generateBannerImage("welcome_illustration.png", "StudentExpress", "Your All-in-One Campus Marketplace");
        generateBannerImage("splash_illustration.png", "StudentExpress", "Connecting Students Across Pune");
    }

    private static void generateImage(String fileName, String title, String subtitle, Color bgColor, Color mainColor, String category) {
        int width = 600;
        int height = 400;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background Gradient
        GradientPaint bgGradient = new GradientPaint(0, 0, bgColor, width, height, new Color(bgColor.getRed()-10, bgColor.getGreen()-10, bgColor.getBlue()-10));
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, width, height);

        // Subtle soft shadow floor
        g2d.setColor(new Color(0, 0, 0, 15));
        g2d.fillOval(100, 280, 400, 70);

        // Product Graphic Element
        g2d.setColor(mainColor);
        switch (category) {
            case "BOOK":
                // Stack of books
                g2d.fill(new RoundRectangle2D.Float(180, 150, 240, 45, 12, 12));
                g2d.setColor(mainColor.darker());
                g2d.fill(new RoundRectangle2D.Float(170, 195, 260, 50, 12, 12));
                g2d.setColor(new Color(79, 119, 45));
                g2d.fill(new RoundRectangle2D.Float(160, 245, 280, 55, 12, 12));
                break;
            case "LAPTOP":
                // Open Laptop
                g2d.fill(new RoundRectangle2D.Float(180, 120, 240, 150, 16, 16));
                g2d.setColor(new Color(220, 230, 240));
                g2d.fill(new RoundRectangle2D.Float(192, 132, 216, 126, 8, 8));
                g2d.setColor(mainColor);
                g2d.fill(new RoundRectangle2D.Float(150, 270, 300, 18, 10, 10));
                break;
            case "TABLE":
                // Desk & Lamp
                g2d.fill(new RoundRectangle2D.Float(150, 200, 300, 22, 6, 6)); // Top
                g2d.fill(new Rectangle2D.Float(170, 222, 20, 90)); // Leg 1
                g2d.fill(new Rectangle2D.Float(410, 222, 20, 90)); // Leg 2
                break;
            case "CYCLE":
                // Bicycle wheels and frame
                g2d.setStroke(new BasicStroke(6));
                g2d.drawOval(150, 180, 100, 100);
                g2d.drawOval(350, 180, 100, 100);
                g2d.drawLine(200, 230, 280, 230);
                g2d.drawLine(280, 230, 340, 170);
                g2d.drawLine(340, 170, 400, 230);
                g2d.drawLine(280, 230, 310, 160);
                break;
            case "AIRPODS":
                // Earbud case
                g2d.fill(new RoundRectangle2D.Float(230, 140, 140, 150, 40, 40));
                g2d.setColor(Color.WHITE);
                g2d.fill(new RoundRectangle2D.Float(240, 150, 120, 130, 30, 30));
                g2d.setColor(new Color(79, 119, 45));
                g2d.fillOval(285, 200, 30, 30);
                break;
            case "BACKPACK":
                // Backpack
                g2d.fill(new RoundRectangle2D.Float(210, 120, 180, 180, 50, 50));
                g2d.setColor(mainColor.brighter());
                g2d.fill(new RoundRectangle2D.Float(230, 180, 140, 100, 20, 20));
                break;
            case "PHONE":
                // Phone outline
                g2d.fill(new RoundRectangle2D.Float(230, 110, 140, 230, 30, 30));
                g2d.setColor(new Color(235, 240, 245));
                g2d.fill(new RoundRectangle2D.Float(238, 118, 124, 214, 22, 22));
                break;
            case "CHAIR":
                // Office chair
                g2d.fill(new RoundRectangle2D.Float(230, 110, 140, 120, 16, 16)); // Back
                g2d.fill(new RoundRectangle2D.Float(210, 230, 180, 25, 10, 10)); // Seat
                g2d.fillRect(290, 255, 20, 50); // Stem
                g2d.fillOval(240, 300, 120, 15); // Base
                break;
        }

        // Overlay Clean Text Label at Bottom
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fill(new RoundRectangle2D.Float(30, 310, 540, 70, 14, 14));
        g2d.setColor(new Color(79, 119, 45));
        g2d.draw(new RoundRectangle2D.Float(30, 310, 540, 70, 14, 14));

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2d.setColor(new Color(31, 41, 55));
        g2d.drawString(title, 50, 342);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2d.setColor(new Color(79, 119, 45));
        g2d.drawString(subtitle, 50, 366);

        g2d.dispose();
        try {
            ImageIO.write(image, "png", new File("assets/image/" + fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateRoomImage(String fileName, String title, String subtitle, Color wallColor, Color bg) {
        int width = 800;
        int height = 550;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Room Background Wall
        g2d.setColor(bg);
        g2d.fillRect(0, 0, width, height);

        // Wall Accent Panel
        g2d.setColor(wallColor);
        g2d.fillRect(0, 0, width, 320);

        // Window with Sunlight
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fill(new RoundRectangle2D.Float(480, 40, 260, 220, 12, 12));
        g2d.setColor(new Color(180, 200, 185));
        g2d.setStroke(new BasicStroke(4));
        g2d.draw(new RoundRectangle2D.Float(480, 40, 260, 220, 12, 12));
        g2d.drawLine(610, 40, 610, 260);
        g2d.drawLine(480, 150, 740, 150);

        // Wooden Floor
        g2d.setColor(new Color(210, 180, 140));
        g2d.fillRect(0, 320, width, 230);
        g2d.setColor(new Color(190, 160, 120));
        for (int i = 0; i < width; i += 60) {
            g2d.drawLine(i, 320, i, 550);
        }

        // Cozy Bed
        g2d.setColor(new Color(120, 80, 40));
        g2d.fill(new RoundRectangle2D.Float(60, 240, 360, 160, 16, 16)); // Bed Frame
        g2d.setColor(new Color(79, 119, 45)); // Green Bedspread
        g2d.fill(new RoundRectangle2D.Float(70, 270, 340, 120, 12, 12));
        g2d.setColor(Color.WHITE); // Pillows
        g2d.fill(new RoundRectangle2D.Float(85, 250, 100, 40, 10, 10));
        g2d.fill(new RoundRectangle2D.Float(200, 250, 100, 40, 10, 10));

        // Indoor Plant
        g2d.setColor(new Color(180, 120, 70));
        g2d.fill(new RoundRectangle2D.Float(440, 300, 50, 60, 10, 10)); // Pot
        g2d.setColor(new Color(79, 119, 45));
        g2d.fillOval(430, 240, 70, 70); // Leaves

        // Text Banner
        g2d.setColor(new Color(255, 255, 255, 235));
        g2d.fill(new RoundRectangle2D.Float(40, 430, 720, 90, 16, 16));
        g2d.setColor(new Color(79, 119, 45));
        g2d.draw(new RoundRectangle2D.Float(40, 430, 720, 90, 16, 16));

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 26));
        g2d.setColor(new Color(31, 41, 55));
        g2d.drawString(title, 70, 470);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2d.setColor(new Color(79, 119, 45));
        g2d.drawString(subtitle, 70, 500);

        g2d.dispose();
        try {
            ImageIO.write(image, "png", new File("assets/image/" + fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateBannerImage(String fileName, String title, String subtitle) {
        int width = 800;
        int height = 450;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, new Color(79, 119, 45), width, height, new Color(46, 75, 25));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.fillOval(500, -50, 400, 400);
        g2d.fillOval(-100, 200, 350, 350);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 42));
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, 80, 200);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        g2d.setColor(new Color(230, 245, 225));
        g2d.drawString(subtitle, 80, 250);

        g2d.dispose();
        try {
            ImageIO.write(image, "png", new File("assets/image/" + fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        generateAllImages();
        System.out.println("All images successfully generated!");
    }
}
