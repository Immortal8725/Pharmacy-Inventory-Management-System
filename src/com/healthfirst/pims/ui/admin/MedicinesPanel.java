package com.healthfirst.pims.ui.admin;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.dao.SupplierDAO;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.model.Supplier;
import com.healthfirst.pims.ui.components.FormSupport;
import com.healthfirst.pims.ui.theme.UITheme;
import com.healthfirst.pims.util.CurrencyUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicinesPanel extends JPanel {

    private static final String[] TYPES = {"Tablet", "Capsule", "Syrup", "Injection", "Cream", "Inhaler", "Powder"};
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final JTextField searchField = FormSupport.textField();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Company", "Type", "Price", "Qty", "Reorder", "Expiry", "Supplier"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public MedicinesPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        header.add(UITheme.heading("Manage medicines"), BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        searchField.setPreferredSize(new java.awt.Dimension(220, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Search medicines...");
        searchField.addActionListener(e -> reload());
        JButton search = UITheme.secondaryButton("Search");
        search.addActionListener(e -> reload());
        JButton add = UITheme.primaryButton("Add medicine");
        add.addActionListener(e -> openEditor(null));
        JButton edit = UITheme.secondaryButton("Edit");
        edit.addActionListener(e -> editSelected());
        JButton delete = UITheme.dangerButton("Delete");
        delete.addActionListener(e -> deleteSelected());
        actions.add(searchField);
        actions.add(search);
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        header.add(actions, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        UITheme.styleTable(table);
        table.setDefaultRenderer(Object.class, new StatusRenderer());
        JScrollPane scroll = UITheme.scroll(table);
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(8, 8, 8, 8)
        ));
        card.add(scroll, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    public void reload() {
        model.setRowCount(0);
        try {
            String term = searchField.getText().trim();
            List<Medicine> medicines = term.isEmpty() ? medicineDAO.findAll() : medicineDAO.search(term);
            for (Medicine medicine : medicines) {
                model.addRow(new Object[]{
                        medicine.getMedicineId(),
                        medicine.getName(),
                        medicine.getCompany(),
                        medicine.getMedicineType(),
                        CurrencyUtil.format(medicine.getPrice()),
                        medicine.getQuantityInStock(),
                        medicine.getReorderLevel(),
                        medicine.getExpiryDate() == null ? "" : DATE.format(medicine.getExpiryDate()),
                        medicine.getSupplierName()
                });
            }
        } catch (SQLException ex) {
            FormSupport.error(this, "Could not load medicines: " + ex.getMessage());
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (Integer) model.getValueAt(row, 0);
    }

    private void editSelected() {
        Integer id = selectedId();
        if (id == null) {
            FormSupport.info(this, "Select a medicine to edit.");
            return;
        }
        try {
            openEditor(medicineDAO.findById(id));
        } catch (SQLException ex) {
            FormSupport.error(this, ex.getMessage());
        }
    }

    private void deleteSelected() {
        Integer id = selectedId();
        if (id == null) {
            FormSupport.info(this, "Select a medicine to delete.");
            return;
        }
        if (!FormSupport.confirm(this, "Delete this medicine from inventory?")) {
            return;
        }
        try {
            medicineDAO.delete(id);
            reload();
        } catch (SQLException ex) {
            FormSupport.error(this, "This medicine cannot be deleted because it is used on existing sales.");
        }
    }

    private void openEditor(Medicine existing) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, existing == null ? "Add medicine" : "Edit medicine", true);
        dialog.setSize(520, 560);
        dialog.setLocationRelativeTo(owner);

        JTextField name = FormSupport.textField();
        JTextField company = FormSupport.textField();
        JComboBox<String> type = new JComboBox<>(TYPES);
        JSpinner price = new JSpinner(new SpinnerNumberModel(0.00, 0.00, 100000.00, 0.50));
        price.setEditor(new JSpinner.NumberEditor(price, "0.00"));
        JSpinner qty = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
        JSpinner reorder = new JSpinner(new SpinnerNumberModel(10, 0, 100000, 1));
        JSpinner expiry = FormSupport.dateSpinner(LocalDate.now().plusYears(1));
        JComboBox<Supplier> supplierBox = new JComboBox<>();

        try {
            for (Supplier supplier : supplierDAO.findAll()) {
                supplierBox.addItem(supplier);
            }
        } catch (SQLException ex) {
            FormSupport.error(this, ex.getMessage());
            return;
        }

        if (existing != null) {
            name.setText(existing.getName());
            company.setText(existing.getCompany());
            type.setSelectedItem(existing.getMedicineType());
            price.setValue(existing.getPrice().doubleValue());
            qty.setValue(existing.getQuantityInStock());
            reorder.setValue(existing.getReorderLevel());
            expiry.setValue(java.sql.Date.valueOf(existing.getExpiryDate()));
            for (int i = 0; i < supplierBox.getItemCount(); i++) {
                if (supplierBox.getItemAt(i).getSupplierId() == existing.getSupplierId()) {
                    supplierBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        form.setBackground(Color.WHITE);
        FormSupport.labeled(form, "Name", name, 0);
        FormSupport.labeled(form, "Company", company, 1);
        FormSupport.labeled(form, "Type", type, 2);
        FormSupport.labeled(form, "Price (ZAR)", price, 3);
        FormSupport.labeled(form, "Quantity", qty, 4);
        FormSupport.labeled(form, "Reorder level", reorder, 5);
        FormSupport.labeled(form, "Expiry date", expiry, 6);
        FormSupport.labeled(form, "Supplier", supplierBox, 7);

        JButton save = UITheme.primaryButton("Save");
        save.addActionListener(e -> {
            if (name.getText().trim().isEmpty()) {
                FormSupport.error(dialog, "Medicine name is required.");
                return;
            }
            if (supplierBox.getSelectedItem() == null) {
                FormSupport.error(dialog, "Select a supplier before saving a medicine.");
                return;
            }
            Medicine medicine = existing == null ? new Medicine() : existing;
            medicine.setName(name.getText().trim());
            medicine.setCompany(company.getText().trim());
            medicine.setMedicineType(String.valueOf(type.getSelectedItem()));
            medicine.setPrice(BigDecimal.valueOf(((Number) price.getValue()).doubleValue())
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            medicine.setQuantityInStock((Integer) qty.getValue());
            medicine.setReorderLevel((Integer) reorder.getValue());
            medicine.setExpiryDate(FormSupport.spinnerDate(expiry));
            medicine.setSupplierId(((Supplier) supplierBox.getSelectedItem()).getSupplierId());
            try {
                if (existing == null) {
                    medicineDAO.insert(medicine);
                } else {
                    medicineDAO.update(medicine);
                }
                dialog.dispose();
                reload();
            } catch (SQLException ex) {
                FormSupport.error(dialog, ex.getMessage());
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBackground(Color.WHITE);
        JButton cancel = UITheme.secondaryButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());
        buttons.add(cancel);
        buttons.add(save);

        dialog.setLayout(new BorderLayout());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                try {
                    int qty = Integer.parseInt(String.valueOf(table.getValueAt(row, 5)));
                    int reorder = Integer.parseInt(String.valueOf(table.getValueAt(row, 6)));
                    LocalDate expiry = LocalDate.parse(String.valueOf(table.getValueAt(row, 7)));
                    if (expiry.isBefore(LocalDate.now()) || !expiry.isAfter(LocalDate.now().plusDays(30))) {
                        c.setBackground(UITheme.WARNING_SOFT);
                    } else if (qty <= reorder) {
                        c.setBackground(UITheme.DANGER_SOFT);
                    } else {
                        c.setBackground(row % 2 == 0 ? UITheme.CARD : UITheme.TABLE_ALT);
                    }
                } catch (Exception ex) {
                    c.setBackground(row % 2 == 0 ? UITheme.CARD : UITheme.TABLE_ALT);
                }
            }
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return c;
        }
    }
}
