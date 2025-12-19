package com.panel;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import com.form.DB;
import com.form.UIStyle;

public class FundingPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton add, edit, del, refresh;
    private String currentUserEmail;

    public FundingPanel(String role, String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;
        setLayout(new BorderLayout());
        model = new DefaultTableModel();
        table = new JTable(model){
       	 @Override
 	    public boolean isCellEditable(int row, int column) {
 	        return false; // disable ALL editing
 	    }
 };

        // columns must match your DB: funder_id, name, Amount, status, CreatedAt, ExperimentID
        model.addColumn("Funder ID");
        model.addColumn("Name");
        model.addColumn("Amount");
        model.addColumn("Status");
        model.addColumn("Created At");
        model.addColumn("ExperimentID");

        add(new JScrollPane(table), BorderLayout.CENTER);

        // header styling
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(135, 206, 250));
        table.getTableHeader().setForeground(Color.BLACK);

        // top controls
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        add = new JButton("Add Funder");
        edit = new JButton("Edit");
        del = new JButton("Delete");
        refresh = new JButton("Refresh");
        UIStyle.styleButton(add);
        UIStyle.styleButton(edit);
        UIStyle.styleButton(del);
        UIStyle.styleButton(refresh);
        UIStyle.stylePanel(top);
        top.add(add); top.add(edit); top.add(del); top.add(refresh);

        // My Profile quick button (keeps your existing behavior)
        JButton myProfileBtn = new JButton("My Profile");
        UIStyle.styleButton(myProfileBtn);
        myProfileBtn.setBackground(new Color(90, 180, 90));
        top.add(myProfileBtn);

        add(top, BorderLayout.NORTH);

        // search area (optional)
        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(200, 28));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("🔍")); searchPanel.add(txtSearch);
        add(searchPanel, BorderLayout.SOUTH);

        // actions
        refresh.addActionListener(e -> load());
        add.addActionListener(e -> addDialog());
        edit.addActionListener(e -> editDialog());
        del.addActionListener(e -> deleteSelected());
        myProfileBtn.addActionListener(e -> {
            // same pattern you used elsewhere
            for (int i = 0; i < table.getRowCount(); i++) {
                // there is no email in fundings; this just demonstrates pattern (you may remove)
                // keep for consistency with other panels:
                // String email = (String) model.getValueAt(i, 2);
                // if (email.equalsIgnoreCase(currentUserEmail)) { ... }
            }
            JOptionPane.showMessageDialog(this, "The My Profile button is present here for consistency.");
        });

        // role-based lock
        if (!"admin".equalsIgnoreCase(role)) {
            add.setEnabled(false);
            edit.setEnabled(false);
            del.setEnabled(false);
        }

        load();

        // simple client-side search on table
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText().trim();
                if (text.length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
    }

    private void load() {
        model.setRowCount(0);
        try (Connection c = DB.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT funder_id, name, Amount, status, CreatedAt, ExperimentID FROM fundings")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("funder_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("Amount"),
                        rs.getString("status"),
                        rs.getTimestamp("CreatedAt"),
                        rs.getObject("ExperimentID")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void addDialog() {
        JTextField name = new JTextField();
        JTextField amount = new JTextField("0.00");
        JComboBox<String> status = new JComboBox<>(new String[]{"Approved","Pending","Declined"});
        JTextField experimentId = new JTextField();

        Object[] msg = {"Name:", name, "Amount:", amount, "Status:", status, "Experiment ID (optional):", experimentId};
        int op = JOptionPane.showConfirmDialog(this, msg, "Add Funder", JOptionPane.OK_CANCEL_OPTION);
        if (op == JOptionPane.OK_OPTION) {
            try (Connection c = DB.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO fundings(name, Amount, status, ExperimentID) VALUES(?,?,?,?)")) {
                ps.setString(1, name.getText().trim());
                ps.setBigDecimal(2, new java.math.BigDecimal(amount.getText().trim()));
                ps.setString(3, (String) status.getSelectedItem());
                if (experimentId.getText().trim().isEmpty()) ps.setNull(4, Types.INTEGER);
                else ps.setInt(4, Integer.parseInt(experimentId.getText().trim()));
                ps.executeUpdate();
                load();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Add: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void editDialog() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a funder to edit"); return; }
        int id = (int) model.getValueAt(r, 0);
        String name0 = String.valueOf(model.getValueAt(r, 1));
        String amount0 = String.valueOf(model.getValueAt(r, 2));
        String status0 = String.valueOf(model.getValueAt(r, 3));
        String exp0 = String.valueOf(model.getValueAt(r, 5));

        JTextField name = new JTextField(name0);
        JTextField amount = new JTextField(amount0);
        JComboBox<String> status = new JComboBox<>(new String[]{"Approved","Pending","Declined"});
        status.setSelectedItem(status0 == null ? "Pending" : status0);
        JTextField experimentId = new JTextField(exp0.equals("null") ? "" : exp0);

        Object[] msg = {"Name:", name, "Amount:", amount, "Status:", status, "Experiment ID (optional):", experimentId};
        int op = JOptionPane.showConfirmDialog(this, msg, "Edit Funder", JOptionPane.OK_CANCEL_OPTION);
        if (op == JOptionPane.OK_OPTION) {
            try (Connection c = DB.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "UPDATE fundings SET name=?, Amount=?, status=?, ExperimentID=? WHERE funder_id=?")) {
                ps.setString(1, name.getText().trim());
                ps.setBigDecimal(2, new java.math.BigDecimal(amount.getText().trim()));
                ps.setString(3, (String) status.getSelectedItem());
                if (experimentId.getText().trim().isEmpty()) ps.setNull(4, Types.INTEGER);
                else ps.setInt(4, Integer.parseInt(experimentId.getText().trim()));
                ps.setInt(5, id);
                ps.executeUpdate();
                load();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Update: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void deleteSelected() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a funder to delete"); return; }
        int id = (int) model.getValueAt(r, 0);
        int c = JOptionPane.showConfirmDialog(this, "Delete funder " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            try (Connection con = DB.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM fundings WHERE funder_id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                load();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
