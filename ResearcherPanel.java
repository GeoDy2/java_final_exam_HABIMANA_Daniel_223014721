package com.panel;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.sql.*;
import com.form.DB;
import com.form.UIStyle;

public class ResearcherPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton addBtn, editBtn, deleteBtn, refreshBtn, exportBtn;
    private String currentUserEmail;
    public ResearcherPanel(String role, String currentUserEmail) {
    	this.currentUserEmail = currentUserEmail;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 245, 255));

        // ========================
        // TABLE & MODEL
        // ========================
        model = new DefaultTableModel(
        	    new Object[]{"ID", "Username", "Full Name", "Email", "Role", "Created At", "Last Login"}, 0
        	) {
        	    @Override
        	    public boolean isCellEditable(int row, int column) {
        	        return false;
        	    }
        	};


        table = new JTable(model){
       	 @Override
 	    public boolean isCellEditable(int row, int column) {
 	        return false; // disable ALL editing
 	    }
 };
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(135, 206, 250));
        table.getTableHeader().setForeground(Color.BLACK);
        table.setSelectionBackground(new Color(200, 220, 255));

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(250, 250, 255));

        // ========================
        // SEARCH PANEL
        // ========================
        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(200, 28));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel lblSearch = new JLabel("🔍 Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        searchPanel.setBackground(new Color(240, 245, 255));
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);

        // ========================
        // BUTTON PANEL
        // ========================
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setBackground(new Color(240, 245, 255));

        addBtn = new JButton("Add");
        editBtn = new JButton("Edit");
        deleteBtn = new JButton("Delete");
        refreshBtn = new JButton("Refresh");
        exportBtn = new JButton("Export CSV");

        JButton[] buttons = {addBtn, editBtn, deleteBtn, refreshBtn, exportBtn};
        for (JButton btn : buttons) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBackground(new Color(60, 120, 215));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(40, 100, 190));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(60, 120, 215));
                }
            });
            top.add(btn);
        }
        JButton myProfileBtn = new JButton("My Profile");
        UIStyle.styleButton(myProfileBtn);
        myProfileBtn.setBackground(new Color(90, 180, 90));

        myProfileBtn.addActionListener(e -> {
            for (int i = 0; i < table.getRowCount(); i++) {
                String email = (String) model.getValueAt(i, 2);
                if (email.equalsIgnoreCase(currentUserEmail)) {
                    table.setRowSelectionInterval(i, i);
                    openEditDialog("user_role_here", currentUserEmail);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Your profile not found in table.");
        });

        // add to the top button bar:
        top.add(myProfileBtn);
        myProfileBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                myProfileBtn.setBackground(new Color(70, 160, 70));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                myProfileBtn.setBackground(new Color(90, 180, 90));
            }
        });



        // ========================
        // BUTTON ACTIONS
        // ========================
        refreshBtn.addActionListener(e -> loadData());
        addBtn.addActionListener(e -> openAddDialog());
      
		editBtn.addActionListener(e -> openEditDialog(role, null));
        deleteBtn.addActionListener(e -> deleteSelected());
        exportBtn.addActionListener(e -> exportCSV());

        // Disable edit/delete for non-admins
        if (!"admin".equalsIgnoreCase(role)) {
            addBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }

        // ========================
        // SEARCH FUNCTIONALITY
        // ========================
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = txtSearch.getText().trim();
                sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        // ========================
        // LAYOUT
        // ========================
//        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
//        add(searchPanel, BorderLayout.SOUTH);
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(240, 245, 255));
        northPanel.add(top, BorderLayout.WEST);       // buttons on the left
        northPanel.add(searchPanel, BorderLayout.EAST); // search on the right
        add(northPanel, BorderLayout.NORTH);


        loadData();
    }

  
	// ========================
    // DATABASE METHODS
    // ========================
    private void loadData() {
        try (Connection c = DB.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(
                 "SELECT ResearcherID, Username, FullName, Email, Role, CreatedAt, LastLogin FROM researcher"
             )) {
            
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("ResearcherID"),
                    rs.getString("Username"),
                    rs.getString("FullName"),
                    rs.getString("Email"),
                    rs.getString("Role"),
                    rs.getString("CreatedAt"),
                    rs.getString("LastLogin")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load error: " + ex.getMessage());
        }
    }


    private void openAddDialog() {
        JTextField username = new JTextField();
        JTextField fullName = new JTextField();
        JTextField email = new JTextField();
        JTextField role = new JTextField();

        Object[] msg = {
            "Username:", username,
            "Full Name:", fullName,
            "Email:", email,
            "Role (Admin/Researcher/Assistant):", role
        };

        int option = JOptionPane.showConfirmDialog(this, msg, "Add a Researcher", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try (Connection c = DB.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO researcher (Username, PasswordHash, Email, FullName, Role) VALUES(?,?,?,?,?)"
                 )) {

                ps.setString(1, username.getText());
                ps.setString(2, "1234"); // default password (you can improve later)
                ps.setString(3, email.getText());
                ps.setString(4, fullName.getText());
                ps.setString(5, role.getText());

                ps.executeUpdate();
                loadData();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Insert error: " + ex.getMessage());
            }
        }
    }


    private void openEditDialog(String role, String currentUserEmail) {
        int r = table.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a row first.");
            return;
        }

        int id = (int) model.getValueAt(r, 0);
        String username0 = (String) model.getValueAt(r, 1);
        String fullName0 = (String) model.getValueAt(r, 2);
        String email0 = (String) model.getValueAt(r, 3);
        String role0 = (String) model.getValueAt(r, 4);

        JTextField username = new JTextField(username0);
        JTextField fullname = new JTextField(fullName0);
        JTextField email = new JTextField(email0);
        JTextField roleField = new JTextField(role0);

        Object[] msg = {
            "Username:", username,
            "Full Name:", fullname,
            "Email:", email,
            "Role:", roleField
        };

        int option = JOptionPane.showConfirmDialog(this, msg, "Edit Researcher", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try (Connection c = DB.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                    "UPDATE researcher SET Username=?, FullName=?, Email=?, Role=? WHERE ResearcherID=?"
                 )) {

                ps.setString(1, username.getText());
                ps.setString(2, fullname.getText());
                ps.setString(3, email.getText());
                ps.setString(4, roleField.getText());
                ps.setInt(5, id);

                ps.executeUpdate();
                loadData();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Update error: " + ex.getMessage());
            }
        }
    }


    private void deleteSelected() {
        int r = table.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a row first.");
            return;
        }
        int id = (int) model.getValueAt(r, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete researcher ID " + id + "?",
                "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection c = DB.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM researcher WHERE researcherID=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete error: " + ex.getMessage());
            }
        }
    }

    private void exportCSV() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save as CSV");
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new java.io.File(file.getAbsolutePath() + ".csv");
                }

                try (FileWriter fw = new FileWriter(file)) {
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        fw.append(table.getColumnName(i));
                        if (i < table.getColumnCount() - 1) fw.append(",");
                    }
                    fw.append("\n");

                    for (int i = 0; i < table.getRowCount(); i++) {
                        for (int j = 0; j < table.getColumnCount(); j++) {
                            Object val = table.getValueAt(i, j);
                            fw.append(val == null ? "" : val.toString());
                            if (j < table.getColumnCount() - 1) fw.append(",");
                        }
                        fw.append("\n");
                    }
                    fw.flush();
                    JOptionPane.showMessageDialog(this, "Export successful!");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error exporting: " + ex.getMessage());
        }
    }
}
