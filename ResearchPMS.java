package com.form;

import com.panel.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.util.Stack;

public class ResearchPMS extends JFrame {
    private String username;
    private String role;
    private int userId;
    private CardLayout cardLayout;
    private JPanel mainArea;
    private Color themeColor = new Color(52, 73, 94); // soft blue
    private JPanel leftNav;
    private Stack<String> history = new Stack<>();
    private Connection conn;
    private Timer slideTimer;
    private boolean menuVisible = false;
    private int navWidth = 200; // width of sidebar when fully open


    public ResearchPMS(String username, String role, int userId, String email) throws Exception {
        this.username = username;
        this.role = role;
        this.userId = userId;
        this.conn = DB.getConnection();

        setTitle("Research Platform Management System - Dashboard");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ======================
        // TOP HEADER BAR
        // ======================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(themeColor);
        header.setPreferredSize(new Dimension(0, 45));

        // Left buttons panel (Back + Menu)
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        leftButtons.setOpaque(false);

        // Back button
        JButton btnBack = createIconButton("/icons/back-button.png", "Back");
        btnBack.addActionListener(e -> goBack());
        leftButtons.add(btnBack);

        // Menu button
        JButton btnMenu = createIconButton("/icons/list.png", "Menu");
        btnMenu.addActionListener(e -> toggleMenu());

        leftButtons.add(btnMenu);

        header.add(leftButtons, BorderLayout.WEST);

        JLabel appName = new JLabel(" Research Platform Management System", JLabel.LEFT);
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/icons/logo.png"));
            Image scaledLogo = logoIcon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
            appName.setIcon(new ImageIcon(scaledLogo));
        } catch (Exception e) {
            System.err.println("⚠️ Logo not found in /icons/logo.png");
        }
        appName.setForeground(Color.WHITE);
        appName.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        header.add(appName, BorderLayout.CENTER);

        JLabel lblStatus = new JLabel("Logged in as: " + username + " | Role: " + role + "  ", JLabel.RIGHT);
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        header.add(lblStatus, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ======================
        // LEFT NAVIGATION PANEL
        // ======================
        leftNav = new JPanel();
        leftNav.setLayout(new GridLayout(0, 1, 8, 8));
        leftNav.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        leftNav.setBackground(new Color(245, 247, 250));

        String[][] buttons = {
            {"Home", "/icons/home.png"},
            {"Researchers", "/icons/Researcher.png"},
            {"Projects", "/icons/Project.png"},
            {"Publications", "/icons/publication.png"},
            {"Datasets","/icons/datasets.png"},
            {"Experiments","/icons/experiment.png"},
            {"Funding", "/icons/funding.png"},
            {"My Profile", "/icons/user.png"},
            {"Logout", "/icons/logout.png"}
        };

        Color normal = new Color(52, 73, 94);
        Color hover = new Color(187, 222, 251);

        for (String[] b : buttons) {
        	if (b == null || b.length < 2) {
                System.err.println("⚠️ Skipping invalid button entry (missing name or icon)");
                continue;
            }
            String name = b[0];
            String iconPath = b[1];
            
            JButton btn = new JButton("  " + name);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setForeground(Color.WHITE);
            btn.setBackground(normal);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setOpaque(true);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 230, 250)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));

            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
                public void mouseExited(MouseEvent e) { btn.setBackground(normal); }
            });

            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
                Image scaled = icon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(scaled));
            } catch (Exception ex) {
                System.err.println("Missing icon: " + iconPath);
            }

            if (name.equals("Users") && !"admin".equalsIgnoreCase(role)) btn.setVisible(false);
            btn.addActionListener(e -> {
				try {
					navAction(name);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
            leftNav.add(btn);
        }

        add(leftNav, BorderLayout.WEST);
        leftNav.setVisible(false); // hide menu on login


        // ======================
        // MAIN AREA WITH CARDS
        // ======================
        cardLayout = new CardLayout();
        mainArea = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(224, 242, 241), // teal tint
                        w, h,
                        new Color(240, 248, 255)); // soft blue-white
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
            }
        };
        mainArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panels
        DashboardPanel dashboardPanel = new DashboardPanel(role, this);
        ResearcherPanel researcherPanel = new ResearcherPanel(role, email);
        ProjectPanel projectPanel = new ProjectPanel(role);
        FundingPanel fundingPanel = new FundingPanel(role, email);
        DatasetPanel datasetPanel = new DatasetPanel(role);
        ExperimentPanel experimentPanel = new ExperimentPanel(role);
        PublicationPanel publicationPanel = new PublicationPanel(role);
        
        MyProfilePanel myProfilePanel = new MyProfilePanel(userId);
	

        // Set names for tracking
        dashboardPanel.setName("Home");
        researcherPanel.setName("Researchers");
        projectPanel.setName("Projects");
        fundingPanel.setName("Fundings");
        datasetPanel.setName("Datasets");
        experimentPanel.setName("Experiments");
        publicationPanel.setName("Publications");
        myProfilePanel.setName("MyProfile");
        
        

        // Transparency
        dashboardPanel.setOpaque(false);
        researcherPanel.setOpaque(false);
        projectPanel.setOpaque(false);
        fundingPanel.setOpaque(false);
        datasetPanel.setOpaque(false);
        experimentPanel.setOpaque(false);
        publicationPanel.setOpaque(false);
       

        // Add panels
        mainArea.add(dashboardPanel, "Home");
        mainArea.add(researcherPanel, "Researchers");
        mainArea.add(projectPanel, "Projects");
        mainArea.add(fundingPanel, "Funding");
        mainArea.add(datasetPanel, "Datasets");
        mainArea.add(experimentPanel, "Experiments");
        mainArea.add(publicationPanel, "Publications");
        mainArea.add(myProfilePanel, "MyProfile");

        add(mainArea, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ======================
    // Utility - Icon Button
    // ======================
    private JButton createIconButton(String path, String tooltip) {
        JButton btn = new JButton();
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(path));
            Image scaled = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            System.err.println("⚠️ Icon not found: " + path);
        }

        return btn;
    }

    // ======================
    // Navigation
    // ======================
    private void navAction(String name) throws Exception{
        if ("Logout".equals(name)) {
            dispose();
            new LoginForm();
            return;
        }
        if ("My Profile".equals(name)) name = "MyProfile";

        String current = getCurrentPanelName();
        if (!history.isEmpty() && !current.equals(history.peek())) {
            history.push(current);
        }

          
    
    if ("Analytics".equals(name)) {
        cardLayout.show(mainArea, "Analytics");
        return;
    }

    // generic case
    cardLayout.show(mainArea, name);
    }
    

    private String getCurrentPanelName() {
        for (Component comp : mainArea.getComponents()) {
            if (comp.isVisible()) return ((JPanel) comp).getName();
        }
        return "Home";
    }

    public void goBack() {
        if (!history.isEmpty()) {
            String prev = history.pop();
            cardLayout.show(mainArea, prev);
        } else {
            cardLayout.show(mainArea, "Home");
        }
    }
   

 // ======================
 // Allow DashboardPanel to switch views
 // ======================
 public void showPanel(String panelName) {
     if (panelName == null || panelName.isEmpty()) return;

     if ("My Profile".equals(panelName)) panelName = "MyProfile";

     String current = getCurrentPanelName();
     if (history.isEmpty() || !current.equals(history.peek())) {
         history.push(current);
     }

     cardLayout.show(mainArea, panelName);
 }
 private void toggleMenu() {
	    int start = menuVisible ? navWidth : 0;
	    int end   = menuVisible ? 0 : navWidth;

	    if (slideTimer != null && slideTimer.isRunning()) return;

	    slideTimer = new Timer(5, null);
	    slideTimer.addActionListener(new ActionListener() {
	        int current = start;

	        @Override
	        public void actionPerformed(ActionEvent e) {
	            int step = 10;

	            if (start < end) {
	                current += step;
	                if (current >= end) {
	                    current = end;
	                    slideTimer.stop();
	                    menuVisible = true;
	                }
	            } else {
	                current -= step;
	                if (current <= end) {
	                    current = end;
	                    slideTimer.stop();
	                    menuVisible = false;
	                }
	            }

	            leftNav.setPreferredSize(new Dimension(current, getHeight()));
	            leftNav.setVisible(current > 0);
	            leftNav.revalidate();
	        }
	    });

	    slideTimer.start();
	}

 private void ensureConnection() throws Exception {
	    if (conn == null) {
	        conn = DB.getConnection();
	        if (conn == null) {
	            JOptionPane.showMessageDialog(this, "Failed to reconnect to database!");
	        }
	    }
	}


}
