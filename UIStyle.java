package com.form;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIStyle {

    // ===== BUTTON STYLE =====
    public static void styleButton(JButton button) {
        if (button == null) return; // prevent crash

        button.setBackground(new Color(30, 136, 229));   // Blue
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(13, 71, 161)),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(30, 136, 229));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(25, 118, 210));
            }
        });
    }

    // ===== PANEL STYLE (for JPanel) =====
    public static void stylePanel(JPanel panel) {
        if (panel == null) return;

        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    // ===== FRAME STYLE (RENAMED) =====
    public static void styleFrame(JFrame frame) {
        if (frame == null) return;
        frame.getContentPane().setBackground(new Color(245, 247, 250));
    }

    // ===== LABELS =====
    public static void styleHeader(JLabel label) {
        if (label == null) return;
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(new Color(33, 33, 33));
    }

    public static void styleText(JTextField txtField) {
        if (txtField == null) return;
        txtField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtField.setForeground(new Color(80, 80, 80));
    }

    // ===== LOGO LABEL =====
    public static JLabel createLogoLabel(String appName, String logoPath) {
        ImageIcon icon = null;
        try {
            if (logoPath != null) {
                icon = new ImageIcon(logoPath);
                Image scaled = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaled);
            }
        } catch (Exception ignored) {}

        JLabel logoLabel = new JLabel(appName, icon, JLabel.LEFT);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(new Color(25, 118, 210));
        logoLabel.setIconTextGap(10);
        return logoLabel;
    }

    // ===== DARK MODE =====
    public static void applyDarkMode(boolean enabled, JPanel panel) {
        if (panel == null) return;
        panel.setBackground(enabled ? new Color(48, 48, 48) : new Color(245, 247, 250));
    }
}

