package com.panel;

import com.form.DB;
import com.form.UIStyle;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MyProfilePanel extends JPanel {
    private int userId;
    private JTextField txtUsername, txtFullName, txtEmail, txtRole;
    private JPasswordField txtPassword;
    private JButton btnEdit, btnUpdate, btnTogglePassword;
    private boolean passwordVisible = false;
    private char defaultEcho;

    public MyProfilePanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblTitle = new JLabel("My Profile");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(33, 150, 243));
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        titlePanel.add(lblTitle);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(20);
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        txtFullName = new JTextField(20);
        formPanel.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        txtRole = new JTextField(20);
        txtRole.setEditable(false);
        formPanel.add(txtRole, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        txtPassword.setEchoChar('•');
        defaultEcho = txtPassword.getEchoChar();
        formPanel.add(txtPassword, gbc);

        // 👁 Toggle show/hide password
        btnTogglePassword = new JButton("Show");
        UIStyle.styleButton(btnTogglePassword);
        btnTogglePassword.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 2;
        formPanel.add(btnTogglePassword, gbc);
        btnTogglePassword.addActionListener(e -> togglePasswordVisibility());

        // buttons
        btnEdit = new JButton("Edit");
        UIStyle.styleButton(btnEdit);
        btnUpdate = new JButton("Update");
        UIStyle.styleButton(btnUpdate);
        btnUpdate.setVisible(false);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnUpdate);

        add(titlePanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load & init
        loadUserData();
        setFieldsEditable(false);

        btnEdit.addActionListener(e -> {
            setFieldsEditable(true);
            btnEdit.setVisible(false);
            btnUpdate.setVisible(true);
        });

        btnUpdate.addActionListener(e -> {
            updateUser();
            setFieldsEditable(false);
            btnUpdate.setVisible(false);
            btnEdit.setVisible(true);
        });
    }

    private void loadUserData() {
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT Username, FullName, Email, Role, PasswordHash FROM researcher WHERE ResearcherID=?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtUsername.setText(rs.getString("Username"));
                    txtFullName.setText(rs.getString("FullName"));
                    txtEmail.setText(rs.getString("Email"));
                    txtRole.setText(rs.getString("Role"));
                    txtPassword.setText(rs.getString("PasswordHash"));
                } else {
                    JOptionPane.showMessageDialog(this, "Profile not found for id " + userId);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateUser() {
        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String passwordHash = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || email.isEmpty() || passwordHash.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, email and password are required.");
            return;
        }

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE researcher SET Username=?, FullName=?, Email=?, PasswordHash=? WHERE ResearcherID=?")) {
            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, email);
            ps.setString(4, passwordHash);
            ps.setInt(5, userId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void togglePasswordVisibility() {
        if (passwordVisible) {
            txtPassword.setEchoChar(defaultEcho);
            btnTogglePassword.setText("Show");
        } else {
            txtPassword.setEchoChar((char) 0);
            btnTogglePassword.setText("Hide");
        }
        passwordVisible = !passwordVisible;
    }

    private void setFieldsEditable(boolean editable) {
        txtUsername.setEditable(editable);
        txtFullName.setEditable(editable);
        txtEmail.setEditable(editable);
        txtPassword.setEditable(editable);
        txtRole.setEditable(false);
    }
}
