package com.healthfirst.pims.ui.cashier;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.ui.components.FormSupport;
import com.healthfirst.pims.ui.theme.UITheme;
import com.healthfirst.pims.util.CurrencyUtil;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
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
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StockCheckPanel extends JPanel {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final JTextField searchField = FormSupport.textField();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Medicine", "Type", "Company", "Price", "In stock", "Expiry"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel nameValue = valueLabel("—");
    private final JLabel priceValue = valueLabel("—");
    private final JLabel stockValue = valueLabel("—");
    private final JLabel expiryValue = valueLabel("—");

    public StockCheckPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.heading("Price and availability"), BorderLayout.WEST);
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchRow.setOpaque(false);
        searchField.setPreferredSize(new Dimension(260, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Search without making a sale");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reload();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reload();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reload();
            }
        });
        JButton refresh = UITheme.secondaryButton("Refresh");
        refresh.addActionListener(e -> reload());
        searchRow.add(searchField);
        searchRow.add(refresh);
        header.add(searchRow, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        UITheme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> showSelected());
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());
        card.add(UITheme.scroll(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
        add(buildDetail(), BorderLayout.SOUTH);
        reload();
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
                        medicine.getMedicineType(),
                        medicine.getCompany(),
                        CurrencyUtil.format(medicine.getPrice()),
                        medicine.getQuantityInStock(),
                        medicine.getExpiryDate() == null ? "" : DATE.format(medicine.getExpiryDate())
                });
            }
        } catch (SQLException ex) {
            FormSupport.error(this, "Could not load stock: " + ex.getMessage());
        }
    }

    private JPanel buildDetail() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setOpaque(false);
        panel.add(stat("Selected medicine", nameValue));
        panel.add(stat("Price", priceValue));
        panel.add(stat("Availability", stockValue));
        panel.add(stat("Expiry", expiryValue));
        return panel;
    }

    private JPanel stat(String title, JLabel value) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 6));
        JLabel caption = new JLabel(title.toUpperCase());
        caption.setFont(new Font("SansSerif", Font.BOLD, 11));
        caption.setForeground(UITheme.MUTED);
        card.add(caption, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private void showSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            nameValue.setText("—");
            priceValue.setText("—");
            stockValue.setText("—");
            expiryValue.setText("—");
            return;
        }
        nameValue.setText(String.valueOf(model.getValueAt(row, 1)));
        priceValue.setText(String.valueOf(model.getValueAt(row, 4)));
        int qty = Integer.parseInt(String.valueOf(model.getValueAt(row, 5)));
        stockValue.setText(qty + (qty > 0 ? " units available" : "  OUT OF STOCK"));
        stockValue.setForeground(qty > 0 ? UITheme.SUCCESS : UITheme.DANGER);
        expiryValue.setText(String.valueOf(model.getValueAt(row, 6)));
    }

    private static JLabel valueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(UITheme.TEXT);
        return label;
    }
}
