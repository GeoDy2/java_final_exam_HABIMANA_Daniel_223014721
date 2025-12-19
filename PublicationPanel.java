package com.panel;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.sql.*;
import com.form.DB;
import com.form.UIStyle;

public class PublicationPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton add, edit, del, refresh;
    private JTextField txtSearch;

    public PublicationPanel(String role) {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

     // ===== TOP BAR: Buttons + Search =====
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Add buttons
        add = new JButton("Add Publication");
        edit = new JButton("Edit");
        del = new JButton("Delete");
        refresh = new JButton("Refresh");
        UIStyle.styleButton(add);
        UIStyle.styleButton(edit);
        UIStyle.styleButton(del);
        UIStyle.styleButton(refresh);

        topBar.add(add);
        topBar.add(Box.createRigidArea(new Dimension(5, 0)));
        topBar.add(edit);
        topBar.add(Box.createRigidArea(new Dimension(5, 0)));
        topBar.add(del);
        topBar.add(Box.createRigidArea(new Dimension(5, 0)));
        topBar.add(refresh);

        // Add horizontal glue to push search bar to the right
        topBar.add(Box.createHorizontalGlue());

        // Search label and field
        JLabel lblSearch = new JLabel("🔍 Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtSearch = new JTextField(20);
        txtSearch.setMaximumSize(new Dimension(220, 28)); // keep consistent height

        topBar.add(lblSearch);
        topBar.add(Box.createRigidArea(new Dimension(5, 0)));
        topBar.add(txtSearch);

        // Add topBar to main panel
        add(topBar, BorderLayout.NORTH);


        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{"ID", "Title", "Journal", "Year", "Project ID"}, 0);
        table = new JTable(model){
       	 @Override
 	    public boolean isCellEditable(int row, int column) {
 	        return false; // disable ALL editing
 	    }
 };
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(144, 202, 249));
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        // ===== EVENT HANDLERS =====
        refresh.addActionListener(e -> load());
        add.addActionListener(e -> addDialog());
        edit.addActionListener(e -> editDialog());
        del.addActionListener(e -> deleteSelected());

        // ===== ROLE CONTROL =====
        if (!(role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("researcher") || role.equalsIgnoreCase("assistant"))) {
            add.setEnabled(false);
            edit.setEnabled(false);
            del.setEnabled(false);
        }

        // ===== SEARCH FEATURE =====
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String text = txtSearch.getText().trim();
                sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        // Load initial data
        load();
    }

    // ===== LOAD DATA =====
    private void load() {
        try (Connection con = DB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT publicationID, Title, Journal, Year, ProjectID FROM publication")) {
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("publicationID"),
                        rs.getString("title"),
                        rs.getString("journal"),
                        rs.getInt("year"),
                        rs.getObject("ProjectID"),
                       
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading publications: " + ex.getMessage());
        }
    }

    // ===== ADD =====
    private void addDialog() {
        JTextField title = new JTextField();
        JTextField journal = new JTextField();
        JTextField year = new JTextField();   
        JTextField project = new JTextField();

        Object[] msg = {
                "Title:", title,
                "Journal:", journal,
                "Year:", year,         
                "Project ID:", project
        };

        int op = JOptionPane.showConfirmDialog(this, msg, "Add Publication", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op == JOptionPane.OK_OPTION) {
            try (Connection con = DB.getConnection();
                 PreparedStatement ps = con.prepareStatement("INSERT INTO publication(title,journal,year,CreatedAt,projectID) VALUES(?,?,?,?,?)")) {

                ps.setString(1, title.getText().trim());
                ps.setString(2, journal.getText().trim());
                ps.setInt(3, Integer.parseInt(year.getText().trim()));
        
                if (project.getText().isEmpty()) ps.setNull(5, Types.INTEGER);
                else ps.setInt(5, Integer.parseInt(project.getText().trim()));

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ Publication added successfully!");
                load();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Add Error: " + ex.getMessage());
            }
        }
    }

    // ===== EDIT =====
    private void editDialog() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }

        int id = (int) model.getValueAt(r, 0);
        JTextField title = new JTextField((String) model.getValueAt(r, 1));
        JTextField journal = new JTextField((String) model.getValueAt(r, 2));
        JTextField year = new JTextField(String.valueOf(model.getValueAt(r, 3)));
        JTextField authors = new JTextField((String) model.getValueAt(r, 4));
        JTextField project = new JTextField(String.valueOf(model.getValueAt(r, 5)));

        Object[] msg = {
                "Title:", title,
                "Journal:", journal,
                "Year:", year,
                "Project ID:", project
        };

        int op = JOptionPane.showConfirmDialog(this, msg, "Edit Publication", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op == JOptionPane.OK_OPTION) {
            try (Connection con = DB.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "UPDATE publication SET Title=?, Journal=?, Year=?, ProjectID=? WHERE publicationID=?")) {

                ps.setString(1, title.getText().trim());
                ps.setString(2, journal.getText().trim());
                ps.setInt(3, Integer.parseInt(year.getText().trim()));
                ps.setString(4, authors.getText().trim());
                if (project.getText().isEmpty()) ps.setNull(5, Types.INTEGER);
                else ps.setInt(5, Integer.parseInt(project.getText().trim()));
                ps.setInt(6, id);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ Publication updated successfully!");
                load();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Update Error: " + ex.getMessage());
            }
        }
    }

    // ===== DELETE =====
    private void deleteSelected() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a publication first."); return; }

        int id = (int) model.getValueAt(r, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete publication #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DB.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM publication WHERE publicationID=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "🗑️ Publication deleted successfully!");
                load();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete Error: " + ex.getMessage());
            }
        }
    }
}
