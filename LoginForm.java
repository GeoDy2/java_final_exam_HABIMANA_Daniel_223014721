package com.form;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;



public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginForm() {
        setTitle("Research Platform - Login");
        setSize(360, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Username
        form.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        form.add(txtUsername);

        // Password
        form.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        txtPassword.setEchoChar('•');

     // STEP 1 — Create a small panel to hold password + eye icon
        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setOpaque(false);
        passPanel.add(txtPassword, BorderLayout.CENTER);

        // STEP 2 — Eye icon button
        JButton btnShow = new JButton();
        btnShow.setBorderPainted(false);
        btnShow.setContentAreaFilled(false);
        btnShow.setFocusPainted(false);
        btnShow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnShow.setPreferredSize(new Dimension(34, 28)); // small width, matches height

        try {
            ImageIcon eyeIcon = new ImageIcon(getClass().getResource("/icons/eye.png"));
            Image scaled = eyeIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            btnShow.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
        	btnShow.setText("👁");
            System.err.println("Missing icon: /icons/eye.png - using text fallback");
        }

        // STEP 2 — Press & hold to show password
        btnShow.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                txtPassword.setEchoChar((char) 0);   // show password
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                txtPassword.setEchoChar('•');        // hide again
            }
        });

        // STEP 3 — Add eye button to right side of password field
        
        passPanel.add(btnShow, BorderLayout.EAST);

        form.add(passPanel);

        UIStyle.styleText(txtUsername);
        UIStyle.styleText(txtPassword);

        // Buttons
        JButton btnLogin = new JButton("Login");
        JButton btnQuit = new JButton("Quit");

        btnLogin.setBackground(new Color(46, 204, 113));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setFocusPainted(false);

        btnQuit.setBackground(new Color(220, 53, 69));
        btnQuit.setForeground(Color.WHITE);
        btnQuit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnQuit.setFocusPainted(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(btnLogin);
        buttons.add(btnQuit);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        UIStyle.styleFrame(this);

        // Actions
        btnLogin.addActionListener(e -> authenticate());
        btnQuit.addActionListener(e -> System.exit(0));
        // Press Enter to login
        getRootPane().setDefaultButton(btnLogin);

        setLocationRelativeTo(null);
        setVisible(true);
        form.setOpaque(false);
    }

    private void authenticate() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password.");
            return;
        }

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                "SELECT ResearcherID, Username, PasswordHash, Role, Email " +
                "FROM researcher WHERE Username=? AND PasswordHash=?")) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    int userId = rs.getInt("ResearcherID");
                    String role = rs.getString("Role");
                    String email = rs.getString("Email");

                    dispose();
                    new ResearchPMS(username, role, userId, email).setVisible(true);

                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.");
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
    }


    public void setBorder(CompoundBorder compoundBorder) {
        // not used
    }
}
