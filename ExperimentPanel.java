package com.panel;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.FileWriter;
import java.sql.*;
import com.form.DB;
import com.form.UIStyle;

public class ExperimentPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    public ExperimentPanel(String role) {
        setLayout(new BorderLayout(10,10));
        setOpaque(false);

        JLabel title = new JLabel("Experiment Management", JLabel.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);
        JButton attachBtn = new JButton("Attach Dataset");
        UIStyle.styleButton(attachBtn);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(attachBtn);
        add(top, BorderLayout.SOUTH);

        attachBtn.addActionListener(e -> attachDataset());


        model = new DefaultTableModel(new String[]{
            "ExperimentID", "Title", "Description", "Methodology", "CreatedAt", "ResearcherID"
        }, 0);
        table = new JTable(model) {
        	 @Override
        	    public boolean isCellEditable(int row, int column) {
        	        return false; // disable ALL editing
        	    }
        };
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        // now scroll is NOT null, safe to style
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(135, 206, 250));
        table.getTableHeader().setForeground(Color.BLACK);

        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        JButton exportBtn = new JButton("Export CSV");
        UIStyle.styleButton(exportBtn);
        UIStyle.stylePanel(top);
        top.add(exportBtn);
        exportBtn.setBackground(new Color(30, 136, 229));
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JButton refreshBtn = new JButton("Refresh");
        UIStyle.styleButton(refreshBtn);
        top.add(refreshBtn);

        refreshBtn.addActionListener(e -> loadData());

        exportBtn.addActionListener(e -> exportCSV());
        add(top, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        try (Connection con = DB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM experiment")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("ExperimentID"),
                    rs.getString("Title"),
                    rs.getString("Description"),
                    rs.getString("Methodology"),
                    rs.getTimestamp("CreatedAt"),
                    rs.getInt("ResearcherID")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading experiments: " + e.getMessage());
        }
    }
    private void exportCSV() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save as CSV");

            int result = chooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) return;

            java.io.File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new java.io.File(file.getAbsolutePath() + ".csv");
            }

            try (FileWriter fw = new FileWriter(file)) {

                // Write column headers
                for (int i = 0; i < table.getColumnCount(); i++) {
                    fw.append(table.getColumnName(i));
                    if (i < table.getColumnCount() - 1) fw.append(",");
                }
                fw.append("\n");

                // Write data
                for (int r = 0; r < table.getRowCount(); r++) {
                    for (int c = 0; c < table.getColumnCount(); c++) {
                        Object value = table.getValueAt(r, c);
                        fw.append(value == null ? "" : value.toString());
                        if (c < table.getColumnCount() - 1) fw.append(",");
                    }
                    fw.append("\n");
                }

                JOptionPane.showMessageDialog(this, "Export completed!");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export Error: " + ex.getMessage());
        }
    }
    private void attachDataset() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an experiment first.");
            return;
        }

        int experimentId = (int) model.getValueAt(row, 0);

        // Load all datasets
        DefaultListModel<String> listModel = new DefaultListModel<>();
        java.util.List<Integer> datasetIds = new java.util.ArrayList<>();

        try (Connection con = DB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT DatasetID, Name FROM dataset")) {

            while (rs.next()) {
                datasetIds.add(rs.getInt("DatasetID"));
                listModel.addElement(rs.getInt("DatasetID") + " - " + rs.getString("Name"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading datasets: " + e.getMessage());
            return;
        }

        JList<String> list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(300, 250));

        int option = JOptionPane.showConfirmDialog(
                this,
                sp,
                "Select Dataset to Attach",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option != JOptionPane.OK_OPTION || list.getSelectedIndex() == -1) return;

        int datasetId = datasetIds.get(list.getSelectedIndex());

        // Check for duplicates
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM experiment_dataset WHERE ExperimentID=? AND DatasetID=?")) {
            ps.setInt(1, experimentId);
            ps.setInt(2, datasetId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Dataset already attached to this experiment.");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error checking duplicate: " + e.getMessage());
            return;
        }

        // Insert new link
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO experiment_dataset (ExperimentID, DatasetID) VALUES (?, ?)")) {
            ps.setInt(1, experimentId);
            ps.setInt(2, datasetId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Dataset successfully attached!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Attach Error: " + e.getMessage());
        }
    }


}
