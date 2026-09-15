package com.healthfirst.pims.ui.components;

import com.healthfirst.pims.ui.theme.UITheme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class StatCard extends JPanel {

    private final JLabel valueLabel;

    public StatCard(String title, String value, String footnote, Color accent) {
        setLayout(new BorderLayout(0, 8));
        setBackground(UITheme.CARD);
        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 6, 0, 0, accent),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        titleLabel.setForeground(UITheme.MUTED);

        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(UITheme.TEXT);

        JLabel note = new JLabel(footnote);
        note.setFont(UITheme.SMALL);
        note.setForeground(accent);

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
        add(note, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }
}
