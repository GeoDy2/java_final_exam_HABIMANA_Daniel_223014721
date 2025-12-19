package com.panel;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.FileWriter;
import java.sql.*;
import com.form.DB;
import com.form.UIStyle;

public class DatasetPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    public DatasetPanel(String role) {
        setLayout(new BorderLayout(10,10));
        setOpaque(false);

        JLabel title = new JLabel("Dataset Management", JLabel.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);
        JButton viewExpBtn = new JButton("View Experiments");
        UIStyle.styleButton(viewExpBtn);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(viewExpBtn);

        add(top, BorderLayout.SOUTH);

        viewExpBtn.addActionListener(e -> viewAssignedExperiments());


        model = new DefaultTableModel(new String[]{
            "DatasetID", "Name", "Description", "Category", "Price/Value", "Status", "CreatedAt", "PublicationID"
        }, 0);
        table = new JTable(model){
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
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
             ResultSet rs = st.executeQuery("SELECT * FROM dataset")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("DatasetID"),
                    rs.getString("Name"),
                    rs.getString("Description"),
                    rs.getString("Category"),
                    rs.getDouble("PriceOrValue"),
                    rs.getString("Status"),
                    rs.getTimestamp("CreatedAt"),
                    rs.getInt("PublicationID")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading dataset: " + e.getMessage());
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
    private void viewAssignedExperiments() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a dataset first.");
            return;
        }

        int datasetId = (int) model.getValueAt(row, 0);

        DefaultTableModel expModel = new DefaultTableModel(
                new String[]{"ExperimentID", "Title", "Methodology", "CreatedAt"}, 0
        );

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT e.ExperimentID, e.Title, e.Methodology, e.CreatedAt " +
                     "FROM experiment_dataset ed " +
                     "JOIN experiment e ON e.ExperimentID = ed.ExperimentID " +
                     "WHERE ed.DatasetID=?")) {

            ps.setInt(1, datasetId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                expModel.addRow(new Object[]{
                        rs.getInt("ExperimentID"),
                        rs.getString("Title"),
                        rs.getString("Methodology"),
                        rs.getTimestamp("CreatedAt")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading experiments: " + e.getMessage());
            return;
        }

        JTable expTable = new JTable(expModel);
        expTable.setRowHeight(25);

        JScrollPane sp = new JScrollPane(expTable);
        sp.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(
                this,
                sp,
                "Experiments Using This Dataset",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


}

