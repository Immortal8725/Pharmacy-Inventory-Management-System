package com.healthfirst.pims.ui.admin;

import com.healthfirst.pims.dao.UserDAO;
import com.healthfirst.pims.model.User;
import com.healthfirst.pims.ui.components.FormSupport;
import com.healthfirst.pims.ui.theme.UITheme;
import com.healthfirst.pims.util.Session;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.sql.SQLException;

public class UsersPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Full name", "Username", "Role"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public UsersPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.heading("Manage users"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton add = UITheme.primaryButton("Add user");
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
        add(UITheme.muted("Create cashier logins for the till. Administrators retain full inventory and report access."),
                BorderLayout.SOUTH);

        UITheme.styleTable(table);
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());
        card.add(UITheme.scroll(table), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.add(card, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    public void reload() {
        model.setRowCount(0);
        try {
            for (User user : userDAO.findAll()) {
                model.addRow(new Object[]{
                        user.getUserId(),
                        user.getFullName(),
                        user.getUsername(),
                        user.getRole()
                });
            }
        } catch (SQLException ex) {
            FormSupport.error(this, "Could not load users: " + ex.getMessage());
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(row, 0);
    }

    private void editSelected() {
        Integer id = selectedId();
        if (id == null) {
            FormSupport.info(this, "Select a user to edit.");
            return;
        }
        try {
            openEditor(userDAO.findById(id));
        } catch (SQLException ex) {
            FormSupport.error(this, ex.getMessage());
        }
    }

    private void deleteSelected() {
        Integer id = selectedId();
        if (id == null) {
            FormSupport.info(this, "Select a user to delete.");
            return;
        }
        if (id == Session.currentUser().getUserId()) {
            FormSupport.error(this, "You cannot delete the account you are signed in with.");
            return;
        }
        if (!FormSupport.confirm(this, "Delete this user account?")) {
            return;
        }
        try {
            userDAO.delete(id);
            reload();
        } catch (SQLException ex) {
            FormSupport.error(this, "This user cannot be deleted because they have processed sales.");
        }
    }

    private void openEditor(User existing) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, existing == null ? "Add user" : "Edit user", true);
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(owner);

        JTextField fullName = FormSupport.textField();
        JTextField username = FormSupport.textField();
        JComboBox<String> role = new JComboBox<>(new String[]{"Cashier", "Admin"});
        JPasswordField password = new JPasswordField();
        password.setFont(UITheme.BODY);

        if (existing != null) {
            fullName.setText(existing.getFullName());
            username.setText(existing.getUsername());
            role.setSelectedItem(existing.getRole());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        FormSupport.labeled(form, "Full name", fullName, 0);
        FormSupport.labeled(form, "Username", username, 1);
        FormSupport.labeled(form, "Role", role, 2);
        FormSupport.labeled(form, existing == null ? "Password" : "New password (optional)", password, 3);

        JButton save = UITheme.primaryButton("Save");
        save.addActionListener(e -> {
            if (fullName.getText().trim().isEmpty() || username.getText().trim().isEmpty()) {
                FormSupport.error(dialog, "Full name and username are required.");
                return;
            }
            String rawPassword = new String(password.getPassword());
            if (existing == null && rawPassword.isBlank()) {
                FormSupport.error(dialog, "A password is required for new users.");
                return;
            }
            try {
                Integer exclude = existing == null ? null : existing.getUserId();
                if (userDAO.usernameExists(username.getText().trim(), exclude)) {
                    FormSupport.error(dialog, "That username is already in use.");
                    return;
                }
                User user = existing == null ? new User() : existing;
                user.setFullName(fullName.getText().trim());
                user.setUsername(username.getText().trim());
                user.setRole(String.valueOf(role.getSelectedItem()));
                if (existing == null) {
                    userDAO.insert(user, rawPassword);
                } else {
                    userDAO.update(user, rawPassword.isBlank() ? null : rawPassword);
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
