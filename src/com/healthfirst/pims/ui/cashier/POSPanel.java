package com.healthfirst.pims.ui.cashier;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.dao.SaleDAO;
import com.healthfirst.pims.model.CartItem;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.ui.components.FormSupport;
import com.healthfirst.pims.ui.theme.UITheme;
import com.healthfirst.pims.util.CurrencyUtil;
import com.healthfirst.pims.util.Session;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class POSPanel extends JPanel {

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final List<CartItem> cart = new ArrayList<>();

    private final JTextField searchField = FormSupport.textField();
    private final DefaultTableModel catalogModel = readOnly("ID", "Medicine", "Type", "Price", "In stock");
    private final JTable catalogTable = new JTable(catalogModel);
    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

    private final DefaultTableModel cartModel = readOnly("Medicine", "Qty", "Unit price", "Line total");
    private final JTable cartTable = new JTable(cartModel);
    private final JLabel totalLabel = new JLabel("R 0.00");

    public POSPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        add(buildCatalog(), BorderLayout.CENTER);
        add(buildCart(), BorderLayout.EAST);
        reloadCatalog();
    }

    public void addDemoItems() {
        try {
            Medicine first = medicineDAO.findById(1);
            Medicine second = medicineDAO.findById(8);
            Medicine third = medicineDAO.findById(12);
            if (first != null) {
                addToCart(first, 2);
            }
            if (second != null) {
                addToCart(second, 3);
            }
            if (third != null) {
                addToCart(third, 1);
            }
        } catch (SQLException ignored) {
            // demo helper only
        }
    }

    private JPanel buildCatalog() {
        JPanel panel = UITheme.card();
        panel.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        JLabel title = UITheme.heading("Medicine catalogue");
        top.add(title, BorderLayout.WEST);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchRow.setOpaque(false);
        searchField.setPreferredSize(new Dimension(240, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Name, company or ID");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reloadCatalog();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reloadCatalog();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reloadCatalog();
            }
        });
        JButton add = UITheme.primaryButton("Add to cart");
        add.addActionListener(e -> addSelected());
        searchRow.add(searchField);
        searchRow.add(new JLabel("Qty"));
        qtySpinner.setPreferredSize(new Dimension(70, 34));
        searchRow.add(qtySpinner);
        searchRow.add(add);
        top.add(searchRow, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        UITheme.styleTable(catalogTable);
        catalogTable.setRowHeight(30);
        panel.add(UITheme.scroll(catalogTable), BorderLayout.CENTER);
        panel.add(UITheme.muted("Cashiers can check prices and sell items. Inventory can only be edited by an administrator."),
                BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCart() {
        JPanel panel = UITheme.card();
        panel.setLayout(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(420, 600));

        JLabel title = UITheme.heading("Current sale");
        panel.add(title, BorderLayout.NORTH);

        UITheme.styleTable(cartTable);
        panel.add(UITheme.scroll(cartTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totalCaption = new JLabel("TOTAL");
        totalCaption.setFont(UITheme.SUBHEADING);
        totalCaption.setForeground(UITheme.MUTED);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        totalLabel.setForeground(UITheme.PRIMARY_DARK);
        totalRow.add(totalCaption, BorderLayout.WEST);
        totalRow.add(totalLabel, BorderLayout.EAST);
        footer.add(totalRow, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
        buttons.setOpaque(false);
        JButton remove = UITheme.secondaryButton("Remove");
        remove.addActionListener(e -> removeSelected());
        JButton clear = UITheme.dangerButton("Clear cart");
        clear.addActionListener(e -> clearCart());
        JButton checkout = UITheme.accentButton("Checkout");
        checkout.addActionListener(e -> checkout());
        buttons.add(remove);
        buttons.add(clear);
        buttons.add(checkout);
        footer.add(buttons, BorderLayout.SOUTH);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private void reloadCatalog() {
        catalogModel.setRowCount(0);
        try {
            String term = searchField.getText().trim();
            List<Medicine> medicines = term.isEmpty() ? medicineDAO.findAll() : medicineDAO.search(term);
            for (Medicine medicine : medicines) {
                catalogModel.addRow(new Object[]{
                        medicine.getMedicineId(),
                        medicine.getName(),
                        medicine.getMedicineType(),
                        CurrencyUtil.format(medicine.getPrice()),
                        medicine.getQuantityInStock()
                });
            }
        } catch (SQLException ex) {
            FormSupport.error(this, "Could not load medicines: " + ex.getMessage());
        }
    }

    private void addSelected() {
        int row = catalogTable.getSelectedRow();
        if (row < 0) {
            FormSupport.info(this, "Select a medicine from the catalogue.");
            return;
        }
        int id = (Integer) catalogModel.getValueAt(row, 0);
        int qty = (Integer) qtySpinner.getValue();
        try {
            Medicine medicine = medicineDAO.findById(id);
            if (medicine == null) {
                FormSupport.error(this, "Medicine no longer exists.");
                return;
            }
            addToCart(medicine, qty);
        } catch (SQLException ex) {
            FormSupport.error(this, ex.getMessage());
        }
    }

    public void addToCart(Medicine medicine, int qty) {
        if (medicine.isExpired()) {
            FormSupport.error(this, medicine.getName() + " has expired and cannot be sold.");
            return;
        }
        int already = 0;
        for (CartItem item : cart) {
            if (item.getMedicine().getMedicineId() == medicine.getMedicineId()) {
                already = item.getQuantity();
                break;
            }
        }
        if (already + qty > medicine.getQuantityInStock()) {
            FormSupport.error(this, "Only " + medicine.getQuantityInStock() + " units of "
                    + medicine.getName() + " are in stock.");
            return;
        }
        for (CartItem item : cart) {
            if (item.getMedicine().getMedicineId() == medicine.getMedicineId()) {
                item.addQuantity(qty);
                refreshCart();
                return;
            }
        }
        cart.add(new CartItem(medicine, qty));
        refreshCart();
    }

    private void removeSelected() {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cart.size()) {
            FormSupport.info(this, "Select a cart line to remove.");
            return;
        }
        cart.remove(row);
        refreshCart();
    }

    public void clearCart() {
        cart.clear();
        refreshCart();
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            cartModel.addRow(new Object[]{
                    item.getMedicine().getName(),
                    item.getQuantity(),
                    CurrencyUtil.format(item.getUnitPrice()),
                    CurrencyUtil.format(item.getLineTotal())
            });
            total = total.add(item.getLineTotal());
        }
        totalLabel.setText(CurrencyUtil.format(total));
    }

    private void checkout() {
        if (cart.isEmpty()) {
            FormSupport.info(this, "Add at least one medicine before checkout.");
            return;
        }
        if (!FormSupport.confirm(this, "Complete this sale for " + totalLabel.getText() + "?")) {
            return;
        }
        try {
            Sale sale = saleDAO.checkout(Session.currentUser().getUserId(), new ArrayList<>(cart));
            List<CartItem> snapshot = new ArrayList<>(cart);
            clearCart();
            reloadCatalog();
            BillDialog.showBill(this, sale, snapshot);
        } catch (SQLException ex) {
            FormSupport.error(this, "Checkout failed: " + ex.getMessage());
        }
    }

    private static DefaultTableModel readOnly(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
