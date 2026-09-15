package com.healthfirst.pims.ui.cashier;

import com.healthfirst.pims.ui.LoginFrame;
import com.healthfirst.pims.ui.theme.UITheme;
import com.healthfirst.pims.util.Session;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

public class CashierDashboard extends JFrame {

    private final POSPanel posPanel = new POSPanel();
    private final StockCheckPanel stockCheckPanel = new StockCheckPanel();
    private final JTabbedPane tabs = new JTabbedPane();

    public CashierDashboard() {
        super("HealthFirst PIMS — Cashier");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        tabs.setFont(UITheme.SUBHEADING);
        tabs.setBorder(new EmptyBorder(8, 12, 12, 12));
        tabs.addTab("Point of Sale", posPanel);
        tabs.addTab("Stock Check", stockCheckPanel);
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == stockCheckPanel) {
                stockCheckPanel.reload();
            }
        });
        add(tabs, BorderLayout.CENTER);
    }

    public POSPanel getPosPanel() {
        return posPanel;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(14, 22, 14, 22));

        JPanel left = new JPanel(new GridLayout(0, 1));
        left.setOpaque(false);
        JLabel brand = new JLabel("HealthFirst  ·  Till");
        brand.setFont(new Font("SansSerif", Font.BOLD, 20));
        brand.setForeground(Color.WHITE);
        JLabel user = new JLabel("Cashier: " + Session.currentUser().getFullName());
        user.setFont(UITheme.SMALL);
        user.setForeground(new Color(153, 246, 228));
        left.add(brand);
        left.add(user);

        JButton logout = UITheme.dangerButton("Sign out");
        logout.addActionListener(e -> {
            Session.logout();
            dispose();
            LoginFrame.showLogin();
        });

        header.add(left, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.PRIMARY),
                new EmptyBorder(14, 22, 14, 22)
        ));
        return header;
    }
}
