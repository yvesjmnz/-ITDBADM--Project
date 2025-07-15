package com.neosburritos.ui.swing;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Modern Swing UI Constants for consistent styling
 * Enhanced version of UIConstants with Swing-specific components
 */
public final class SwingUIConstants {
    
    // Color Palette - Modern Material Design inspired
    public static final Color PRIMARY_COLOR = new Color(46, 139, 87);      // Forest Green
    public static final Color PRIMARY_DARK = new Color(34, 104, 65);       // Darker Green
    public static final Color PRIMARY_LIGHT = new Color(76, 175, 80);      // Lighter Green
    public static final Color SECONDARY_COLOR = new Color(255, 140, 0);    // Dark Orange
    public static final Color BACKGROUND_COLOR = new Color(248, 249, 250); // Light Gray
    public static final Color SURFACE_COLOR = Color.WHITE;
    public static final Color ERROR_COLOR = new Color(244, 67, 54);        // Material Red
    public static final Color SUCCESS_COLOR = new Color(76, 175, 80);      // Material Green
    public static final Color WARNING_COLOR = new Color(255, 193, 7);      // Material Amber
    public static final Color INFO_COLOR = new Color(33, 150, 243);        // Material Blue
    public static final Color TEXT_PRIMARY = new Color(33, 37, 41);        // Dark Gray
    public static final Color TEXT_SECONDARY = new Color(108, 117, 125);   // Medium Gray
    public static final Color BORDER_COLOR = new Color(222, 226, 230);     // Light Border
    public static final Color HOVER_COLOR = new Color(240, 240, 240);      // Light Hover
    
    // Typography - Modern font stack
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font CAPTION_FONT = new Font("Segoe UI", Font.ITALIC, 11);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    
    // Spacing - Consistent spacing system
    public static final int PADDING_TINY = 4;
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 16;
    public static final int PADDING_LARGE = 24;
    public static final int PADDING_XLARGE = 32;
    
    // Component Dimensions
    public static final Dimension BUTTON_SIZE = new Dimension(120, 36);
    public static final Dimension LARGE_BUTTON_SIZE = new Dimension(150, 42);
    public static final Dimension TEXT_FIELD_SIZE = new Dimension(200, 32);
    public static final Dimension LARGE_TEXT_FIELD_SIZE = new Dimension(300, 32);
    public static final Dimension COMBO_BOX_SIZE = new Dimension(200, 32);
    
    // Window Dimensions
    public static final int MAIN_WINDOW_WIDTH = 1400;
    public static final int MAIN_WINDOW_HEIGHT = 900;
    public static final int DIALOG_WIDTH = 450;
    public static final int DIALOG_HEIGHT = 250;
    
    // Border Styles
    public static final int BORDER_RADIUS = 8;
    public static final int BORDER_THICKNESS = 1;
    public static final Border PANEL_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR, 1),
        BorderFactory.createEmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
    );
    public static final Border CARD_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR, 1),
        BorderFactory.createEmptyBorder(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE)
    );
    
    private SwingUIConstants() {
        // Prevent instantiation
    }
    
    /**
     * Create a modern styled button with consistent appearance
     */
    public static JButton createStyledButton(String text, Color backgroundColor, Color textColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(textColor);
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(BUTTON_SIZE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(backgroundColor.darker());
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(backgroundColor);
                }
            }
        });
        
        return button;
    }
    
    /**
     * Create a primary action button
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR, Color.WHITE);
    }
    
    /**
     * Create a secondary action button
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = createStyledButton(text, SURFACE_COLOR, TEXT_PRIMARY);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return button;
    }
    
    /**
     * Create a success button
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS_COLOR, Color.WHITE);
    }
    
    /**
     * Create a warning button
     */
    public static JButton createWarningButton(String text) {
        return createStyledButton(text, WARNING_COLOR, TEXT_PRIMARY);
    }
    
    /**
     * Create a danger button
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, ERROR_COLOR, Color.WHITE);
    }
    
    /**
     * Create a styled text field
     */
    public static JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(BODY_FONT);
        field.setBackground(SURFACE_COLOR);
        field.setForeground(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL)
        ));
        field.setPreferredSize(TEXT_FIELD_SIZE);
        return field;
    }
    
    /**
     * Create a styled text area
     */
    public static JTextArea createStyledTextArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        area.setFont(BODY_FONT);
        area.setBackground(SURFACE_COLOR);
        area.setForeground(TEXT_PRIMARY);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL)
        ));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }
    
    /**
     * Create a styled combo box
     */
    public static JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(BODY_FONT);
        comboBox.setBackground(SURFACE_COLOR);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setPreferredSize(COMBO_BOX_SIZE);
        return comboBox;
    }
    
    /**
     * Create a styled label
     */
    public static JLabel createStyledLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }
    
    /**
     * Create a title label
     */
    public static JLabel createTitleLabel(String text) {
        return createStyledLabel(text, TITLE_FONT, PRIMARY_COLOR);
    }
    
    /**
     * Create a subtitle label
     */
    public static JLabel createSubtitleLabel(String text) {
        return createStyledLabel(text, SUBTITLE_FONT, TEXT_PRIMARY);
    }
    
    /**
     * Create a header label
     */
    public static JLabel createHeaderLabel(String text) {
        return createStyledLabel(text, HEADER_FONT, TEXT_PRIMARY);
    }
    
    /**
     * Create a body label
     */
    public static JLabel createBodyLabel(String text) {
        return createStyledLabel(text, BODY_FONT, TEXT_PRIMARY);
    }
    
    /**
     * Create a secondary text label
     */
    public static JLabel createSecondaryLabel(String text) {
        return createStyledLabel(text, BODY_FONT, TEXT_SECONDARY);
    }
    
    /**
     * Create a styled panel with background color
     */
    public static JPanel createStyledPanel(Color backgroundColor) {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        return panel;
    }
    
    /**
     * Create a card panel with border and padding
     */
    public static JPanel createCardPanel() {
        JPanel panel = createStyledPanel(SURFACE_COLOR);
        panel.setBorder(CARD_BORDER);
        return panel;
    }
    
    /**
     * Create a section panel with title
     */
    public static JPanel createSectionPanel(String title) {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout());
        
        JLabel titleLabel = createHeaderLabel(title);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, PADDING_MEDIUM, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        return panel;
    }
    
    /**
     * Show modern error dialog
     */
    public static void showErrorDialog(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show modern success dialog
     */
    public static void showSuccessDialog(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show modern warning dialog
     */
    public static void showWarningDialog(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Show modern confirmation dialog
     */
    public static boolean showConfirmDialog(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(
            parent, message, title, 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
}