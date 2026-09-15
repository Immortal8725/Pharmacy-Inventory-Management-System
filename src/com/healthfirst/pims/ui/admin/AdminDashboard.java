package com.healthfirst.pims.ui.admin;

import com.healthfirst.pims.ui.LoginFrame;
import com.healthfirst.pims.ui.components.NavButton;
import com.healthfirst.pims.ui.theme.UITheme;
import com.healthfirst.pims.util.Session;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminDashboard extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();
    private final OverviewPanel overviewPanel = new OverviewPanel();
    private final MedicinesPanel medicinesPanel = new MedicinesPanel();
    private final SuppliersPanel suppliersPanel = new SuppliersPanel();
    private final UsersPanel usersPanel = new UsersPanel();
    private final ReportsPanel reportsPanel = new ReportsPanel();

    public AdminDashboard() {
        super("HealthFirst PIMS — Administrator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildShell(), BorderLayout.CENTER);
        showPage("overview");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UITheme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 800));
        sidebar.setBorder(new EmptyBorder(24, 16, 16, 16));

        JPanel brand = new JPanel(new GridLayout(0, 1, 0, 2));
        brand.setOpaque(false);
        JLabel name = new JLabel("HealthFirst");
        name.setFont(new Font("SansSerif", Font.BOLD, 22));
        name.setForeground(Color.WHITE);
        JLabel role = new JLabel("Administrator");
        role.setFont(UITheme.SMALL);
        role.setForeground(new Color(153, 246, 228));
        brand.add(name);
        brand.add(role);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel(new GridLayout(0, 1, 0, 8));
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(28, 0, 0, 0));
        addNav(nav, "overview", "Overview");
        addNav(nav, "medicines", "Manage Medicines");
        addNav(nav, "suppliers", "Manage Suppliers");
        addNav(nav, "users", "Manage Users");
        addNav(nav, "reports", "Reports");
        sidebar.add(nav, BorderLayout.CENTER);

        JButton logout = UITheme.dangerButton("Sign out");
        logout.setPreferredSize(new Dimension(198, 38));
        logout.addActionListener(e -> {
            Session.logout();
            dispose();
            LoginFrame.showLogin();
        });
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(logout);
        sidebar.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNav(JPanel nav, String key, String label) {
        NavButton button = new NavButton("  " + label);
        button.addActionListener(e -> showPage(key));
        navButtons.put(key, button);
        nav.add(button);
    }

    private JPanel buildShell() {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(UITheme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(14, 22, 14, 22)
        ));
        JLabel greeting = new JLabel("Welcome, " + Session.currentUser().getFullName());
        greeting.setFont(UITheme.SUBHEADING);
        greeting.setForeground(UITheme.TEXT);
        JLabel meta = new JLabel("Signed in as " + Session.currentUser().getUsername() + "  ·  Inventory & reporting");
        meta.setFont(UITheme.SMALL);
        meta.setForeground(UITheme.MUTED);
        JPanel left = new JPanel(new GridLayout(0, 1));
        left.setOpaque(false);
        left.add(greeting);
        left.add(meta);
        header.add(left, BorderLayout.WEST);

        content.setOpaque(false);
        content.setBorder(new EmptyBorder(18, 18, 18, 18));
        content.add(overviewPanel, "overview");
        content.add(medicinesPanel, "medicines");
        content.add(suppliersPanel, "suppliers");
        content.add(usersPanel, "users");
        content.add(reportsPanel, "reports");

        shell.add(header, BorderLayout.NORTH);
        shell.add(content, BorderLayout.CENTER);
        return shell;
    }

    public void showPage(String key) {
        cards.show(content, key);
        navButtons.forEach((name, button) -> button.setActive(name.equals(key)));
        switch (key) {
            case "overview" -> overviewPanel.reload();
            case "medicines" -> medicinesPanel.reload();
            case "suppliers" -> suppliersPanel.reload();
            case "users" -> usersPanel.reload();
            case "reports" -> reportsPanel.reload();
            default -> {
            }
        }
    }
}
