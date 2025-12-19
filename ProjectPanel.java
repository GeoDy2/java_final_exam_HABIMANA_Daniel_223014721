package com.panel;

import javax.swing.*;
import com.form.UIStyle;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.FileWriter;
import java.sql.*;
import com.form.DB;

public class ProjectPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton add, edit, del, refresh;

    public ProjectPanel(String role) {
        setLayout(new BorderLayout());
        model = new DefaultTableModel();
        table = new JTable(model){
       	 @Override
 	    public boolean isCellEditable(int row, int column) {
 	        return false; // disable ALL editing
 	    }
 };
        model.addColumn("ProjectID");
        model.addColumn("Title");
        model.addColumn("Description");
        model.addColumn("Status");
        model.addColumn("Created At");
        model.addColumn("ResearcherID");

        add(new JScrollPane(table), BorderLayout.CENTER);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(135, 206, 250));
        table.getTableHeader().setForeground(Color.BLACK);
        JScrollPane scroll = (JScrollPane) table.getParent().getParent();
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        // 🔹 Top action bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        add = new JButton("Add Project");
        edit = new JButton("Edit");
        del = new JButton("Delete");
        refresh = new JButton("Refresh");
        JButton exportBtn = new JButton("Export CSV");
        UIStyle.styleButton(add);
        UIStyle.styleButton(edit);
        UIStyle.styleButton(del);
        UIStyle.styleButton(refresh);
        UIStyle.styleButton(exportBtn);
        UIStyle.stylePanel(top);
        top.add(add); top.add(edit); top.add(del); top.add(refresh); top.add(exportBtn);
        add(top, BorderLayout.NORTH);

        // 🔹 Action listeners
        refresh.addActionListener(e -> load());
        add.addActionListener(e -> addDialog());
        edit.addActionListener(e -> editDialog());
        del.addActionListener(e -> deleteSelected());
        exportBtn.addActionListener(e ->exportCSV());

        // 🔹 Role-based permissions
        if (!("admin".equalsIgnoreCase(role))) {
            add.setEnabled(false);
            edit.setEnabled(false);
            del.setEnabled(false);
        }

        // 🔹 Search Panel (Top Right)
        JTextField txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(200, 28));
        JLabel lblSearch = new JLabel("🔍 Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);

        // Combine top and search panel
        JPanel combinedTop = new JPanel(new BorderLayout());
        combinedTop.add(top, BorderLayout.WEST);
        combinedTop.add(searchPanel, BorderLayout.EAST);
        add(combinedTop, BorderLayout.NORTH);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = txtSearch.getText().trim();
                if (text.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        // Load data initially
        load();
    }

    private void load() {
        try (Connection c = DB.getConnection();
             Statement s = c.createStatement();
        		ResultSet rs = s.executeQuery("SELECT ProjectID, Title, Description, Status, CreatedAt, ResearcherID FROM project")) {
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("projectID"),
                        rs.getString("Title"),
                        rs.getObject("Description"),
                        rs.getString("Status"),
                        rs.getTimestamp("CreatedAt"),
                        rs.getString("ResearcherID")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load error Project: " + ex.getMessage());
        }
    }

    private void addDialog() {
        JTextField title = new JTextField();
        JTextField desc = new JTextField();
        JTextField status = new JTextField("Ongoing");
        JTextField researcherID = new JTextField();

        Object[] msg = {
                "Title:", title,
                "Description:", desc,
                "Status:", status,
                "Researcher ID:", researcherID
        };

        int op = JOptionPane.showConfirmDialog(this, msg, "Add Project", JOptionPane.OK_CANCEL_OPTION);

        if (op == JOptionPane.OK_OPTION) {
            try (Connection c = DB.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO project (Title, Description, Status, ResearcherID) VALUES (?, ?, ?, ?)")) {

                ps.setString(1, title.getText());
                ps.setString(2, desc.getText());
                ps.setString(3, status.getText());

                if (researcherID.getText().trim().isEmpty()) {
                    ps.setNull(4, Types.INTEGER);
                } else {
                    ps.setInt(4, Integer.parseInt(researcherID.getText().trim()));
                }

                ps.executeUpdate();
                load();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Add error: " + ex.getMessage());
            }
        }
    }


    private void editDialog() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a project to edit"); return; }

        int id = (int) model.getValueAt(r, 0);
        String title0 = (String) model.getValueAt(r, 1);
        String sd0 = String.valueOf(model.getValueAt(r, 2));
        String status0 = (String) model.getValueAt(r, 3);
        String researcher0 = String.valueOf(model.getValueAt(r, 5));

        JTextField title = new JTextField(title0);
        JTextField desc = new JTextField(sd0.equals("null") ? "" : sd0);
        JTextField status = new JTextField(status0);
        JTextField researcherID = new JTextField(researcher0.equals("null") ? "" : researcher0);

        Object[] msg = {"Title:", title, "Description:", desc, "Status:", status, "Researcher ID:", researcherID};
        int op = JOptionPane.showConfirmDialog(this, msg, "Edit Project", JOptionPane.OK_CANCEL_OPTION);

        if (op == JOptionPane.OK_OPTION) {
            try (Connection c = DB.getConnection();
                 PreparedStatement ps = c.prepareStatement("UPDATE project SET Title=?,ResearcherID=?,Description=?,CreatedAt=?,status=? WHERE projectID=?")) {
            	 ps.setString(1, title.getText());
                 ps.setString(2, desc.getText());
                 ps.setString(3, status.getText());

                 if (researcherID.getText().trim().isEmpty())
                     ps.setNull(4, Types.INTEGER);
                 else
                     ps.setInt(4, Integer.parseInt(researcherID.getText().trim()));

                 ps.setInt(5, id);

                 ps.executeUpdate();
                 load();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Update error: " + ex.getMessage());
            }
        }
    }

    private void deleteSelected() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a project to delete"); return; }

        int id = (int) model.getValueAt(r, 0);
        int c = JOptionPane.showConfirmDialog(this, "Delete project " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            try (Connection con = DB.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM project WHERE projectID=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                load();
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
