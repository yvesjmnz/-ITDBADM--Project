package com.neosburritos.ui;

import java.awt.*;

/**
 * UI Constants for consistent styling across the application
 * Follows single responsibility principle for design consistency
 */
public final class UIConstants {
    
    // Color Palette
    public static final Color PRIMARY_COLOR = new Color(46, 139, 87);      // Forest Green
    public static final Color PRIMARY_DARK = new Color(34, 104, 65);       // Darker Green
    public static final Color SECONDARY_COLOR = new Color(255, 140, 0);    // Dark Orange
    public static final Color BACKGROUND_COLOR = new Color(248, 249, 250); // Light Gray
    public static final Color SURFACE_COLOR = Color.WHITE;
    public static final Color ERROR_COLOR = new Color(220, 53, 69);        // Red
    public static final Color SUCCESS_COLOR = new Color(40, 167, 69);      // Green
    public static final Color WARNING_COLOR = new Color(255, 193, 7);      // Yellow
    public static final Color TEXT_PRIMARY = new Color(33, 37, 41);        // Dark Gray
    public static final Color TEXT_SECONDARY = new Color(108, 117, 125);   // Medium Gray
    public static final Color BORDER_COLOR = new Color(222, 226, 230);     // Light Border
    
    // Typography
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);
    public static final Font SUBTITLE_FONT = new Font("Arial", Font.BOLD, 18);
    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font BODY_FONT = new Font("Arial", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("Arial", Font.PLAIN, 12);
    public static final Font CAPTION_FONT = new Font("Arial", Font.ITALIC, 11);
    
    // Spacing
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 16;
    public static final int PADDING_LARGE = 24;
    public static final int PADDING_XLARGE = 32;
    
    // Component Dimensions
    public static final Dimension BUTTON_SIZE = new Dimension(120, 35);
    public static final Dimension LARGE_BUTTON_SIZE = new Dimension(150, 40);
    public static final Dimension TEXT_FIELD_SIZE = new Dimension(200, 30);
    public static final Dimension LARGE_TEXT_FIELD_SIZE = new Dimension(300, 30);
    
    // Window Dimensions
    public static final int MAIN_WINDOW_WIDTH = 1200;
    public static final int MAIN_WINDOW_HEIGHT = 800;
    public static final int DIALOG_WIDTH = 400;
    public static final int DIALOG_HEIGHT = 200;
    
    // Border Styles
    public static final int BORDER_RADIUS = 8;
    public static final int BORDER_THICKNESS = 1;
    
    private UIConstants() {
        // Prevent instantiation
    }
    
    /**
     * Create a styled button with consistent appearance
     */
    public static Button createStyledButton(String text, Color backgroundColor) {
        Button button = new Button(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(BODY_FONT);
        button.setPreferredSize(BUTTON_SIZE);
        return button;
    }
    
    /**
     * Create a primary action button
     */
    public static Button createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR);
    }
    
    /**
     * Create a secondary action button
     */
    public static Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setBackground(SURFACE_COLOR);
        button.setForeground(TEXT_PRIMARY);
        button.setFont(BODY_FONT);
        button.setPreferredSize(BUTTON_SIZE);
        return button;
    }
    
    /**
     * Create a styled text field
     */
    public static TextField createStyledTextField(int columns) {
        TextField field = new TextField(columns);
        field.setFont(BODY_FONT);
        field.setBackground(SURFACE_COLOR);
        field.setForeground(TEXT_PRIMARY);
        return field;
    }
    
    /**
     * Create a styled label
     */
    public static Label createStyledLabel(String text, Font font, Color color) {
        Label label = new Label(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }
    
    /**
     * Create a title label
     */
    public static Label createTitleLabel(String text) {
        return createStyledLabel(text, TITLE_FONT, PRIMARY_COLOR);
    }
    
    /**
     * Create a header label
     */
    public static Label createHeaderLabel(String text) {
        return createStyledLabel(text, HEADER_FONT, TEXT_PRIMARY);
    }
    
    /**
     * Create a body label
     */
    public static Label createBodyLabel(String text) {
        return createStyledLabel(text, BODY_FONT, TEXT_PRIMARY);
    }
    
    /**
     * Create a styled panel with background color
     */
    public static Panel createStyledPanel(Color backgroundColor) {
        Panel panel = new Panel();
        panel.setBackground(backgroundColor);
        return panel;
    }
}