package com.healthfirst.pims.ui.components;

import com.healthfirst.pims.ui.theme.UITheme;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

public class NavButton extends JButton {

    private boolean active;

    public NavButton(String text) {
        super(text);
        setHorizontalAlignment(SwingConstants.LEFT);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(true);
        setOpaque(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(210, 42));
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setInactive();
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            setBackground(UITheme.ACCENT);
            setForeground(Color.WHITE);
        } else {
            setInactive();
        }
    }

    public boolean isActive() {
        return active;
    }

    private void setInactive() {
        setBackground(UITheme.SIDEBAR);
        setForeground(new Color(204, 251, 241));
    }
}
