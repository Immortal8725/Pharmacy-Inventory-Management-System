package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.UserDAO;
import com.healthfirst.pims.model.User;
import com.healthfirst.pims.ui.admin.AdminDashboard;
import com.healthfirst.pims.ui.cashier.CashierDashboard;
import com.healthfirst.pims.ui.theme.UITheme;
import com.healthfirst.pims.util.Session;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel errorLabel = new JLabel(" ");
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        super("HealthFirst PIMS — Sign in");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(980, 620);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(buildBrandPanel(), BorderLayout.WEST);
        add(buildFormPanel(), BorderLayout.CENTER);
        getRootPane().setDefaultButton(null);
        usernameField.addActionListener(this::attemptLogin);
        passwordField.addActionListener(this::attemptLogin);
    }

    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(-80, getHeight() - 220, 280, 280, 80, 80);
                g2.setColor(UITheme.ACCENT);
                g2.fillRoundRect(getWidth() - 140, -60, 200, 200, 70, 70);
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(420, 620));
        panel.setBorder(new EmptyBorder(48, 40, 40, 40));
        panel.setOpaque(false);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new javax.swing.BoxLayout(inner, javax.swing.BoxLayout.Y_AXIS));

        JPanel cross = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(18, 4, 12, 40, 6, 6);
                g2.fillRoundRect(4, 18, 40, 12, 6, 6);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(48, 48);
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        cross.setOpaque(false);
        cross.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(cross);
        inner.add(javax.swing.Box.createVerticalStrut(18));

        JLabel name = new JLabel("HealthFirst");
        name.setFont(new Font("SansSerif", Font.BOLD, 32));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(name);

        JLabel tag = new JLabel("Pharmacy Inventory Management");
        tag.setFont(new Font("SansSerif", Font.PLAIN, 15));
        tag.setForeground(new Color(204, 251, 241));
        tag.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(tag);

        inner.add(javax.swing.Box.createVerticalStrut(22));
        JLabel copy = new JLabel("<html><div style='width:260px;line-height:1.45'>"
                + "Secure stock control, fast checkout, and clear business reports "
                + "for HealthFirst Pharmacy.</div></html>");
        copy.setFont(UITheme.BODY);
        copy.setForeground(new Color(226, 232, 240));
        copy.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(copy);

        panel.add(inner, BorderLayout.NORTH);

        JLabel footer = new JLabel("PRO732  ·  Desktop Edition");
        footer.setForeground(new Color(148, 163, 184));
        footer.setFont(UITheme.SMALL);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(380, 420));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(6, 0, 6, 0);

        JLabel title = new JLabel("Sign in");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.TEXT);
        form.add(title, c);

        JLabel subtitle = new JLabel("Use your HealthFirst staff account");
        subtitle.setFont(UITheme.BODY);
        subtitle.setForeground(UITheme.MUTED);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 18, 0);
        form.add(subtitle, c);

        c.gridy = 2;
        c.insets = new Insets(8, 0, 4, 0);
        form.add(label("Username"), c);
        usernameField.setFont(UITheme.BODY);
        usernameField.setPreferredSize(new Dimension(320, 40));
        c.gridy = 3;
        c.insets = new Insets(0, 0, 10, 0);
        form.add(usernameField, c);

        c.gridy = 4;
        c.insets = new Insets(8, 0, 4, 0);
        form.add(label("Password"), c);
        passwordField.setFont(UITheme.BODY);
        passwordField.setPreferredSize(new Dimension(320, 40));
        c.gridy = 5;
        c.insets = new Insets(0, 0, 8, 0);
        form.add(passwordField, c);

        errorLabel.setForeground(UITheme.DANGER);
        errorLabel.setFont(UITheme.SMALL);
        errorLabel.setHorizontalAlignment(SwingConstants.LEFT);
        c.gridy = 6;
        form.add(errorLabel, c);

        JButton login = UITheme.primaryButton("Sign in");
        login.setPreferredSize(new Dimension(320, 44));
        login.addActionListener(this::attemptLogin);
        c.gridy = 7;
        c.insets = new Insets(12, 0, 8, 0);
        form.add(login, c);

        JLabel hint = new JLabel("<html>Admin: <b>admin / admin123</b><br>Cashier: <b>cashier / cash123</b></html>");
        hint.setFont(UITheme.SMALL);
        hint.setForeground(UITheme.MUTED);
        hint.setBorder(new EmptyBorder(16, 0, 0, 0));
        c.gridy = 8;
        form.add(hint, c);

        wrap.add(form);
        wrap.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return wrap;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.SMALL);
        label.setForeground(UITheme.MUTED);
        return label;
    }

    private void attemptLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        errorLabel.setText(" ");

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Enter both username and password.");
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user == null) {
                errorLabel.setText("Invalid username or password. Please try again.");
                passwordField.setText("");
                passwordField.requestFocusInWindow();
                return;
            }
            Session.login(user);
            dispose();
            if (user.isAdmin()) {
                new AdminDashboard().setVisible(true);
            } else {
                new CashierDashboard().setVisible(true);
            }
        } catch (SQLException ex) {
            errorLabel.setText("Unable to reach the database. " + ex.getMessage());
        }
    }

    public static void showLogin() {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
