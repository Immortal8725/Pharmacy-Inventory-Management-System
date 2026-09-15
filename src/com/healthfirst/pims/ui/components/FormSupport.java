package com.healthfirst.pims.ui.components;

import com.healthfirst.pims.ui.theme.UITheme;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import java.awt.Component;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public final class FormSupport {

    private FormSupport() {
    }

    public static JTextField textField() {
        JTextField field = new JTextField(20);
        field.setFont(UITheme.BODY);
        return field;
    }

    public static JSpinner dateSpinner(LocalDate initial) {
        Date value = Date.from(initial.atStartOfDay(ZoneId.systemDefault()).toInstant());
        SpinnerDateModel model = new SpinnerDateModel(value, null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        spinner.setFont(UITheme.BODY);
        return spinner;
    }

    public static LocalDate spinnerDate(JSpinner spinner) {
        Date date = (Date) spinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "HealthFirst PIMS", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "HealthFirst PIMS", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void labeled(JPanel form, String label, Component field, int y) {
        java.awt.GridBagConstraints labelC = new java.awt.GridBagConstraints();
        labelC.gridx = 0;
        labelC.gridy = y;
        labelC.anchor = java.awt.GridBagConstraints.WEST;
        labelC.insets = new java.awt.Insets(8, 8, 8, 12);
        javax.swing.JLabel jLabel = new javax.swing.JLabel(label);
        jLabel.setFont(UITheme.BODY);
        form.add(jLabel, labelC);

        java.awt.GridBagConstraints fieldC = new java.awt.GridBagConstraints();
        fieldC.gridx = 1;
        fieldC.gridy = y;
        fieldC.fill = java.awt.GridBagConstraints.HORIZONTAL;
        fieldC.weightx = 1;
        fieldC.insets = new java.awt.Insets(8, 0, 8, 8);
        form.add(field, fieldC);
    }
}
