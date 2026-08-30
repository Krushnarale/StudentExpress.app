package com.core2web.util;

public class Theme {
    // Core Color Palette
    public static final String PRIMARY        = "#4F772D";
    public static final String PRIMARY_DARK   = "#2E4A18";
    public static final String PRIMARY_HOVER  = "#3E6023";
    public static final String PRIMARY_LIGHT  = "#EFF4EB";
    public static final String BADGE_BG       = "#E9F2E6";
    public static final String BADGE_TEXT     = "#3B672B";
    public static final String BG_COLOR       = "#F4F6F0";
    public static final String CARD_BG        = "#FFFFFF";
    public static final String BORDER_COLOR   = "#DDE5D8";
    public static final String TEXT_PRIMARY   = "#1A2515";
    public static final String TEXT_MUTED     = "#6B7280";

    // Font Family
    public static final String FONT = "'Segoe UI', 'Inter', 'Roboto', 'Arial', sans-serif";

    // Root & Background
    public static String rootPaneStyle() {
        return "-fx-background-color: " + BG_COLOR + ";";
    }

    // Top Navigation Bar
    public static String topBarStyle() {
        return "-fx-background-color: " + CARD_BG + ";"
             + "-fx-border-color: " + BORDER_COLOR + ";"
             + "-fx-border-width: 0 0 1 0;"
             + "-fx-effect: dropshadow(gaussian, rgba(79,119,45,0.08), 8, 0, 0, 2);";
    }

    // Sidebar
    public static String sidebarStyle() {
        return "-fx-background-color: linear-gradient(to bottom, " + PRIMARY_DARK + ", #1E3310);"
             + "-fx-border-color: rgba(255, 255, 255, 0.12);"
             + "-fx-border-width: 0 1 0 0;";
    }

    public static String sidebarSectionLabelStyle() {
        return "-fx-text-fill: rgba(255, 255, 255, 0.62);"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 11.5px;"
             + "-fx-font-weight: 800;"
             + "-fx-padding: 4px 0 3px 10px;";
    }

    // Sidebar Buttons
    public static String sidebarBtnStyle(boolean isActive) {
        if (isActive) {
            return "-fx-background-color: " + PRIMARY_LIGHT + ";"
                 + "-fx-text-fill: " + PRIMARY_DARK + ";"
                 + "-fx-font-family: " + FONT + ";"
                 + "-fx-font-weight: 800;"
                 + "-fx-font-size: 15.5px;"
                 + "-fx-padding: 7.5px 12px;"
                 + "-fx-background-radius: 10px;"
                 + "-fx-border-color: transparent transparent transparent #8BC34A;"
                 + "-fx-border-width: 0 0 0 4px;"
                 + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 6, 0, 0, 2);"
                 + "-fx-cursor: hand;";
        }
        return "-fx-background-color: transparent;"
             + "-fx-text-fill: rgba(255, 255, 255, 0.90);"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-weight: 600;"
             + "-fx-font-size: 15.5px;"
             + "-fx-padding: 7.5px 12px;"
             + "-fx-background-radius: 10px;"
             + "-fx-cursor: hand;";
    }

    public static String sidebarBtnHoverStyle() {
        return "-fx-background-color: rgba(255, 255, 255, 0.14);"
             + "-fx-text-fill: #FFFFFF;"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-weight: 700;"
             + "-fx-font-size: 15.5px;"
             + "-fx-padding: 7.5px 12px;"
             + "-fx-background-radius: 10px;"
             + "-fx-cursor: hand;";
    }

    // Logo Text
    public static String logoTextStyle() {
        return "-fx-fill: " + PRIMARY + ";"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 26px;"
             + "-fx-font-weight: 800;"
             + "-fx-cursor: hand;";
    }

    // Search Field
    public static String searchFieldStyle() {
        return "-fx-background-color: " + BG_COLOR + ";"
             + "-fx-border-color: " + BORDER_COLOR + ";"
             + "-fx-border-radius: 24px;"
             + "-fx-background-radius: 24px;"
             + "-fx-padding: 9px 18px;"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 14px;"
             + "-fx-prompt-text-fill: " + TEXT_MUTED + ";";
    }

    // Profile Button
    public static String profileBtnStyle() {
        return "-fx-background-color: " + PRIMARY_LIGHT + ";"
             + "-fx-text-fill: " + PRIMARY + ";"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-weight: 700;"
             + "-fx-font-size: 14px;"
             + "-fx-border-color: " + BORDER_COLOR + ";"
             + "-fx-border-radius: 20px;"
             + "-fx-background-radius: 20px;"
             + "-fx-padding: 8px 18px;"
             + "-fx-cursor: hand;";
    }

    // Standard Card
    public static String cardStyle() {
        return "-fx-background-color: " + CARD_BG + ";"
             + "-fx-border-color: " + BORDER_COLOR + ";"
             + "-fx-border-radius: 14px;"
             + "-fx-background-radius: 14px;"
             + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);";
    }

    // Elevated Card (more visible shadow)
    public static String elevatedCardStyle() {
        return "-fx-background-color: " + CARD_BG + ";"
             + "-fx-border-color: " + BORDER_COLOR + ";"
             + "-fx-border-radius: 16px;"
             + "-fx-background-radius: 16px;"
             + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 6);";
    }

    // Stat Card with colored left accent bar
    public static String statCardStyle(String accentColor) {
        return "-fx-background-color: " + CARD_BG + ";"
             + "-fx-border-color: " + accentColor + " " + BORDER_COLOR + " " + BORDER_COLOR + " " + accentColor + ";"
             + "-fx-border-width: 0 0 0 4px;"
             + "-fx-border-radius: 14px;"
             + "-fx-background-radius: 14px;"
             + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 12, 0, 0, 4);";
    }

    // Primary Button
    public static String primaryBtnStyle() {
        return "-fx-background-color: linear-gradient(to bottom, " + PRIMARY + ", " + PRIMARY_HOVER + ");"
             + "-fx-text-fill: #FFFFFF;"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-weight: 700;"
             + "-fx-font-size: 13px;"
             + "-fx-padding: 10px 20px;"
             + "-fx-background-radius: 10px;"
             + "-fx-cursor: hand;"
             + "-fx-effect: dropshadow(gaussian, rgba(79,119,45,0.3), 8, 0, 0, 3);";
    }

    // Secondary Button
    public static String secondaryBtnStyle() {
        return "-fx-background-color: " + PRIMARY_LIGHT + ";"
             + "-fx-text-fill: " + PRIMARY + ";"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-weight: 700;"
             + "-fx-font-size: 13px;"
             + "-fx-padding: 10px 20px;"
             + "-fx-background-radius: 10px;"
             + "-fx-border-color: " + BORDER_COLOR + ";"
             + "-fx-border-radius: 10px;"
             + "-fx-cursor: hand;";
    }

    // Outline Button
    public static String outlineBtnStyle() {
        return "-fx-background-color: " + CARD_BG + ";"
             + "-fx-text-fill: " + TEXT_PRIMARY + ";"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-weight: 600;"
             + "-fx-font-size: 12px;"
             + "-fx-border-color: " + BORDER_COLOR + ";"
             + "-fx-border-radius: 20px;"
             + "-fx-background-radius: 20px;"
             + "-fx-padding: 7px 16px;"
             + "-fx-cursor: hand;";
    }

    // Active Outline Pill Button
    public static String activePillBtnStyle() {
        return "-fx-background-color: " + PRIMARY + ";"
             + "-fx-text-fill: white;"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-weight: 700;"
             + "-fx-font-size: 12px;"
             + "-fx-border-radius: 20px;"
             + "-fx-background-radius: 20px;"
             + "-fx-padding: 7px 16px;"
             + "-fx-cursor: hand;"
             + "-fx-effect: dropshadow(gaussian, rgba(79,119,45,0.3), 6, 0, 0, 2);";
    }

    // Badge
    public static String badgeStyle() {
        return "-fx-background-color: " + BADGE_BG + ";"
             + "-fx-text-fill: " + BADGE_TEXT + ";"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 11px;"
             + "-fx-font-weight: 700;"
             + "-fx-padding: 3px 10px;"
             + "-fx-background-radius: 20px;";
    }

    // Status Badges
    public static String successBadgeStyle() {
        return "-fx-background-color: #E6F4EA; -fx-text-fill: #2E7D32;"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 11px; -fx-font-weight: 700;"
             + "-fx-padding: 3px 10px; -fx-background-radius: 20px;";
    }

    public static String warningBadgeStyle() {
        return "-fx-background-color: #FFF8E1; -fx-text-fill: #F57F17;"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 11px; -fx-font-weight: 700;"
             + "-fx-padding: 3px 10px; -fx-background-radius: 20px;";
    }

    public static String dangerBadgeStyle() {
        return "-fx-background-color: #FFF5F5; -fx-text-fill: #C62828;"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 11px; -fx-font-weight: 700;"
             + "-fx-padding: 3px 10px; -fx-background-radius: 20px;";
    }

    // Filter Pill
    public static String filterPillStyle(boolean isActive) {
        if (isActive) {
            return activePillBtnStyle();
        }
        return outlineBtnStyle();
    }

    // Title Text
    public static String titleTextStyle() {
        return "-fx-fill: " + TEXT_PRIMARY + ";"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 26px;"
             + "-fx-font-weight: 800;";
    }

    // Section Header Text
    public static String sectionHeaderStyle() {
        return "-fx-fill: " + TEXT_PRIMARY + ";"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 18px;"
             + "-fx-font-weight: 700;";
    }

    // Price Text
    public static String priceTextStyle() {
        return "-fx-fill: " + PRIMARY + ";"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 17px;"
             + "-fx-font-weight: 800;";
    }

    // Muted Body Text
    public static String mutedTextStyle() {
        return "-fx-fill: " + TEXT_MUTED + ";"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 13px;";
    }

    // Danger Delete Button
    public static String dangerBtnStyle() {
        return "-fx-background-color: #FFF5F5;"
             + "-fx-text-fill: #C62828;"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-weight: 700;"
             + "-fx-font-size: 13px;"
             + "-fx-border-color: #FEB2B2;"
             + "-fx-border-radius: 10px;"
             + "-fx-background-radius: 10px;"
             + "-fx-padding: 10px 16px;"
             + "-fx-cursor: hand;";
    }

    // Green Gradient Background Panel
    public static String gradientPanelStyle() {
        return "-fx-background-color: linear-gradient(to bottom right, " + PRIMARY + ", " + PRIMARY_DARK + ");"
             + "-fx-background-radius: 14px;";
    }

    // Input Field Style
    public static String inputFieldStyle() {
        return "-fx-background-color: " + BG_COLOR + ";"
             + "-fx-border-color: " + BORDER_COLOR + ";"
             + "-fx-border-radius: 10px;"
             + "-fx-background-radius: 10px;"
             + "-fx-padding: 12px 14px;"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 13px;"
             + "-fx-prompt-text-fill: " + TEXT_MUTED + ";";
    }

    // ComboBox Style
    public static String comboBoxStyle() {
        return "-fx-background-color: " + CARD_BG + ";"
             + "-fx-border-color: " + BORDER_COLOR + ";"
             + "-fx-border-radius: 20px;"
             + "-fx-background-radius: 20px;"
             + "-fx-padding: 4px 10px;"
             + "-fx-font-family: " + FONT + ";"
             + "-fx-font-size: 12px;"
             + "-fx-font-weight: 600;"
             + "-fx-cursor: hand;";
    }
}

