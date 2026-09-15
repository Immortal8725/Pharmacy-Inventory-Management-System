package com.healthfirst.pims.ui.theme;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class UITheme {

    public static final Color PRIMARY = new Color(13, 92, 89);
    public static final Color PRIMARY_DARK = new Color(8, 56, 54);
    public static final Color ACCENT = new Color(15, 157, 140);
    public static final Color ACCENT_SOFT = new Color(204, 251, 241);
    public static final Color BACKGROUND = new Color(241, 245, 249);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(15, 23, 42);
    public static final Color MUTED = new Color(100, 116, 139);
    public static final Color DANGER = new Color(185, 28, 28);
    public static final Color DANGER_SOFT = new Color(254, 226, 226);
    public static final Color WARNING = new Color(180, 83, 9);
    public static final Color WARNING_SOFT = new Color(254, 243, 199);
    public static final Color SUCCESS = new Color(4, 120, 87);
    public static final Color SUCCESS_SOFT = new Color(209, 250, 229);
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color TABLE_ALT = new Color(248, 250, 252);
    public static final Color SIDEBAR = new Color(8, 47, 46);

    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 26);
    public static final Font HEADING = new Font("SansSerif", Font.BOLD, 20);
    public static final Font SUBHEADING = new Font("SansSerif", Font.BOLD, 15);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font SMALL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font BUTTON = new Font("SansSerif", Font.BOLD, 13);

    private UITheme() {
    }

    public static void install() {
        FlatLightLaf.setup();
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("TabbedPane.selectedBackground", CARD);
        UIManager.put("TabbedPane.underlineColor", ACCENT);
        UIManager.put("TabbedPane.selectedForeground", PRIMARY);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.selectionBackground", new Color(204, 251, 241));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("Button.default.background", PRIMARY);
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("defaultFont", BODY);
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 38));
        return button;
    }

    public static JButton accentButton(String text) {
        JButton button = primaryButton(text);
        button.setBackground(ACCENT);
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON);
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 38));
        return button;
    }

    public static JButton dangerButton(String text) {
        JButton button = primaryButton(text);
        button.setBackground(DANGER);
        return button;
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADING);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY);
        label.setForeground(MUTED);
        return label;
    }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(BODY);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        JTableHeader header = table.getTableHeader();
        header.setFont(BUTTON);
        header.setForeground(PRIMARY_DARK);
        header.setBackground(new Color(236, 253, 245));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new StripeRenderer());
    }

    public static JScrollPane scroll(JComponent component) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(CARD);
        return scroll;
    }

    public static class StripeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                component.setBackground(row % 2 == 0 ? CARD : TABLE_ALT);
            }
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return component;
        }
    }

    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;

        public RoundedPanel(int radius, Color fill) {
            this.radius = radius;
            this.fill = fill;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
