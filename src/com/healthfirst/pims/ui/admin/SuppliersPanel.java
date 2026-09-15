package com.healthfirst.pims.ui.admin;

import com.healthfirst.pims.dao.SupplierDAO;
import com.healthfirst.pims.model.Supplier;
import com.healthfirst.pims.ui.components.FormSupport;
import com.healthfirst.pims.ui.theme.UITheme;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.sql.SQLException;

public class SuppliersPanel extends JPanel {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Contact person", "Phone", "Email", "Address"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public SuppliersPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.heading("Manage suppliers"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton add = UITheme.primaryButton("Add supplier");
        add.addActionListener(e -> openEditor(null));
        JButton edit = UITheme.secondaryButton("Edit");
        edit.addActionListener(e -> editSelected());
        JButton delete = UITheme.dangerButton("Delete");
        delete.addActionListener(e -> deleteSelected());
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        UITheme.styleTable(table);
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());
        card.add(UITheme.scroll(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    public void reload() {
        model.setRowCount(0);
        try {
            for (Supplier supplier : supplierDAO.findAll()) {
                model.addRow(new Object[]{
                        supplier.getSupplierId(),
                        supplier.getName(),
                        supplier.getContactPerson(),
                        supplier.getPhone(),
                        supplier.getEmail(),
                        supplier.getAddress()
                });
            }
        } catch (SQLException ex) {
            FormSupport.error(this, "Could not load suppliers: " + ex.getMessage());
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    private void editSelected() {
        Integer id = selectedId();
        if (id == null) {
            FormSupport.info(this, "Select a supplier to edit.");
            return;
        }
        try {
            openEditor(supplierDAO.findById(id));
        } catch (SQLException ex) {
            FormSupport.error(this, ex.getMessage());
        }
    }

    private void deleteSelected() {
        Integer id = selectedId();
        if (id == null) {
            FormSupport.info(this, "Select a supplier to delete.");
            return;
        }
        if (!FormSupport.confirm(this, "Delete this supplier?")) {
            return;
        }
        try {
            supplierDAO.delete(id);
            reload();
        } catch (SQLException ex) {
            FormSupport.error(this, "This supplier cannot be deleted while medicines still reference it.");
        }
    }

    private void openEditor(Supplier existing) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, existing == null ? "Add supplier" : "Edit supplier", true);
        dialog.setSize(520, 460);
        dialog.setLocationRelativeTo(owner);

        JTextField name = FormSupport.textField();
        JTextField contact = FormSupport.textField();
        JTextField phone = FormSupport.textField();
        JTextField email = FormSupport.textField();
        JTextArea address = new JTextArea(4, 20);
        address.setLineWrap(true);
        address.setWrapStyleWord(true);
        address.setFont(UITheme.BODY);
        JScrollPane addressScroll = new JScrollPane(address);
        addressScroll.setPreferredSize(new Dimension(240, 80));

        if (existing != null) {
            name.setText(existing.getName());
            contact.setText(existing.getContactPerson());
            phone.setText(existing.getPhone());
            email.setText(existing.getEmail());
            address.setText(existing.getAddress());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        FormSupport.labeled(form, "Name", name, 0);
        FormSupport.labeled(form, "Contact person", contact, 1);
        FormSupport.labeled(form, "Phone", phone, 2);
        FormSupport.labeled(form, "Email", email, 3);
        FormSupport.labeled(form, "Address", addressScroll, 4);

        JButton save = UITheme.primaryButton("Save");
        save.addActionListener(e -> {
            if (name.getText().trim().isEmpty()) {
                FormSupport.error(dialog, "Supplier name is required.");
                return;
            }
            Supplier supplier = existing == null ? new Supplier() : existing;
            supplier.setName(name.getText().trim());
            supplier.setContactPerson(contact.getText().trim());
            supplier.setPhone(phone.getText().trim());
            supplier.setEmail(email.getText().trim());
            supplier.setAddress(address.getText().trim());
            try {
                if (existing == null) {
                    supplierDAO.insert(supplier);
                } else {
                    supplierDAO.update(supplier);
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
}
