package com.panel;

import com.form.DB;
import com.form.ResearchPMS;
import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DashboardPanel extends JPanel {

    private ResearchPMS parentFrame;
    private JLabel lblDatasetPerExp;
    private JLabel lblExpPerDataset;


    public DashboardPanel(String role, ResearchPMS parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(20, 20));
        setOpaque(false); // gradient background

        // ===========================
        // SEARCH BAR
        // ===========================
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(250, 36));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 150, 255), 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        txtSearch.setBackground(new Color(250, 250, 255));
        txtSearch.setToolTipText("Search across the system...");
        
        txtSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a keyword to search.");
                return;
            }

            StringBuilder results = new StringBuilder();
            try (Connection con = DB.getConnection()) {
                if (con == null) {
                    JOptionPane.showMessageDialog(this, "Database connection failed!");
                    return;
                }

                // Each entry: query + label of table
                String[][] searches = {
                	    {"SELECT FullName AS result FROM researcher WHERE FullName LIKE ?", "Researchers"},
                	    {"SELECT Title AS result FROM project WHERE Title LIKE ?", "Projects"},
                	    {"SELECT name AS result FROM fundings WHERE name LIKE ?", "Funding"},
                	    {"SELECT Title AS result FROM publication WHERE Title LIKE ?", "Publications"},
                	    {"SELECT Name AS result FROM dataset WHERE Name LIKE ?", "Datasets"},
                	    {"SELECT Title AS result FROM experiment WHERE Title LIKE ?", "Experiments"},
                   };

                for (String[] pair : searches) {
                    String query = pair[0];
                    String table = pair[1];

                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        ps.setString(1, "%" + keyword + "%");
                        ResultSet rs = ps.executeQuery();
                        boolean found = false;
                        while (rs.next()) {
                            if (!found) {
                                results.append("\n📘 From ").append(table).append(":\n");
                                found = true;
                            }
                            results.append("  • ").append(rs.getString("result")).append("\n");
                        }
                    }
                }

                if (results.length() == 0) {
                    JOptionPane.showMessageDialog(this, "No results found for: " + keyword);
                } else {
                    JTextArea txtResults = new JTextArea(results.toString());
                    txtResults.setEditable(false);
                    txtResults.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    JScrollPane scroll = new JScrollPane(txtResults);
                    scroll.setPreferredSize(new Dimension(450, 300));
                    JOptionPane.showMessageDialog(this, scroll, "Results for '" + keyword + "'", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });


            

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setBackground(new Color(245, 247, 255));
        searchWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 180, 255), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        searchWrapper.add(searchIcon, BorderLayout.WEST);
        searchWrapper.add(txtSearch, BorderLayout.CENTER);

        topBar.add(searchWrapper, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        // ===========================
        // DASHBOARD CARDS
        // ===========================
        JPanel grid = new JPanel(new GridLayout(2, 4, 20, 20));
        grid.setOpaque(false);
        JButton analyticsCard = new JButton("View Analytics");
        analyticsCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        analyticsCard.addActionListener(e -> {
            // parentFrame is passed from ResearchPMS constructor
            parentFrame.showPanel("Analytics");
        });


        String[] tables = { "researcher","project","fundings","publication","dataset","experiment"};

        for (String table : tables) {
            int count = getCount(table);
            JPanel card = createCard(table, count);
            grid.add(card);
        }

        add(grid, BorderLayout.CENTER);
     // ===========================
     // RELATIONSHIP STATISTICS
     // ===========================
     JPanel stats = new JPanel(new GridLayout(1, 2, 20, 20));
     stats.setOpaque(false);

     lblDatasetPerExp = new JLabel("Loading...", JLabel.CENTER);
     lblDatasetPerExp.setFont(new Font("Segoe UI", Font.BOLD, 16));
     lblDatasetPerExp.setForeground(new Color(80, 90, 120));

     lblExpPerDataset = new JLabel("Loading...", JLabel.CENTER);
     lblExpPerDataset.setFont(new Font("Segoe UI", Font.BOLD, 16));
     lblExpPerDataset.setForeground(new Color(80, 90, 120));

     stats.add(lblDatasetPerExp);
     stats.add(lblExpPerDataset);

     add(stats, BorderLayout.SOUTH);

     // Load relational counts
     loadRelationshipStats();

    }

    private JPanel createCard(String table, int count) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(new Color(247, 249, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 230, 245), 1),
                BorderFactory.createEmptyBorder(20, 10, 20, 10)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblTitle = new JLabel(table.substring(0, 1).toUpperCase() + table.substring(1), JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        lblTitle.setForeground(new Color(40, 60, 100));

        JLabel lblCount = new JLabel(String.valueOf(count), JLabel.CENTER);
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblCount.setForeground(new Color(25, 80, 160));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblCount, BorderLayout.CENTER);
        

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(227, 242, 253));
                card.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 230), 3));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(new Color(255, 255, 255, 230));
                card.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 240), 2));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            	String panelName = switch (table) {
                case "researcher" -> "Researchers";
                case "project" -> "Projects";
                case "fundings" -> "Funding";
                case "publication" -> "Publications";
                case "dataset" -> "Datasets";
                case "experiment" -> "Experiments";
                default -> null;
            };

            if (panelName != null) {
                parentFrame.showPanel(panelName);
            }


            }
        });

        return card;
    }

    private int getCount(String table) {
        int count = 0;
        try (Connection con = DB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception ex) {
            System.err.println("DB error on " + table + ": " + ex.getMessage());
        }
        return count;
    }
    private void loadRelationshipStats() {
        int expDatasetLinks = countDatasetsForExperiments();
        lblDatasetPerExp.setText("Total Dataset Assignments: " + expDatasetLinks);

        int datasetExpLinks = countExperimentsForDatasets();
        lblExpPerDataset.setText("Total Experiment Assignments: " + datasetExpLinks);
    }
    private int countExperimentsForDatasets() {
        try (Connection con = DB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) AS total FROM experiment_dataset"
             )) {

            if (rs.next()) return rs.getInt("total");

        } catch (Exception e) {
            System.out.println("Dashboard Error (experiments per dataset): " + e.getMessage());
        }
        return 0;
    }



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth(), h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 255),
                0, h, new Color(232, 240, 243));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
    }
    private int countDatasetsForExperiments() {
        try (Connection con = DB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) AS total " +
                     "FROM experiment_dataset"
             )) {

            if (rs.next()) return rs.getInt("total");

        } catch (Exception e) {
            System.out.println("Dashboard Error (datasets per experiment): " + e.getMessage());
        }
        return 0;
    }
    

}
