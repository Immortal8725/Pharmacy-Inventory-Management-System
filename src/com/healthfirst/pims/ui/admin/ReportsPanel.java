package com.healthfirst.pims.ui.admin;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.dao.SaleDAO;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.ui.components.FormSupport;
import com.healthfirst.pims.ui.theme.UITheme;
import com.healthfirst.pims.util.CurrencyUtil;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsPanel extends JPanel {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final SaleDAO saleDAO = new SaleDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    private final JSpinner fromSpinner = FormSupport.dateSpinner(LocalDate.now().withDayOfMonth(1));
    private final JSpinner toSpinner = FormSupport.dateSpinner(LocalDate.now());
    private final JLabel salesSummary = UITheme.muted(" ");
    private final JTabbedPane tabs = new JTabbedPane();

    private final DefaultTableModel salesModel = readOnly(
            "Sale #", "Date", "Cashier", "Items", "Total");
    private final DefaultTableModel itemModel = readOnly(
            "ID", "Medicine", "Type", "Units sold", "Revenue");
    private final DefaultTableModel lowModel = readOnly(
            "ID", "Medicine", "Qty", "Reorder level", "Supplier");
    private final DefaultTableModel expiryModel = readOnly(
            "ID", "Medicine", "Type", "Qty", "Expiry date", "Supplier");

    public ReportsPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        add(UITheme.heading("Business reports"), BorderLayout.NORTH);

        tabs.setFont(UITheme.SUBHEADING);
        tabs.addTab("Sales Report", buildSalesTab());
        tabs.addTab("Item-Wise Report", buildItemTab());
        tabs.addTab("Low Stock Report", buildLowStockTab());
        tabs.addTab("Expiry Report", buildExpiryTab());
        add(tabs, BorderLayout.CENTER);
    }

    public void selectTab(int index) {
        tabs.setSelectedIndex(index);
    }

    public void reload() {
        refreshSales();
        refreshItemWise();
        refreshLowStock();
        refreshExpiry();
    }

    private JPanel buildSalesTab() {
        JPanel panel = wrapTable(salesModel);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);
        filters.add(new JLabel("From"));
        filters.add(fromSpinner);
        filters.add(new JLabel("To"));
        filters.add(toSpinner);
        JButton run = UITheme.primaryButton("Run report");
        run.addActionListener(e -> {
            refreshSales();
            refreshItemWise();
        });
        filters.add(run);
        filters.add(salesSummary);
        panel.add(filters, BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildItemTab() {
        JPanel panel = wrapTable(itemModel);
        panel.add(UITheme.muted("Units sold and revenue by medicine for the selected date range."), BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildLowStockTab() {
        JPanel panel = wrapTable(lowModel);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UITheme.muted("Medicines at or below their reorder level. Restock these first."), BorderLayout.WEST);
        JButton refresh = UITheme.secondaryButton("Refresh");
        refresh.addActionListener(e -> refreshLowStock());
        top.add(refresh, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildExpiryTab() {
        JPanel panel = wrapTable(expiryModel);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UITheme.muted("Medicines expiring within the next 30 days."), BorderLayout.WEST);
        JButton refresh = UITheme.secondaryButton("Refresh");
        refresh.addActionListener(e -> refreshExpiry());
        top.add(refresh, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);
        return panel;
    }

    private JPanel wrapTable(DefaultTableModel model) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 8, 8, 8));
        JTable table = new JTable(model);
        UITheme.styleTable(table);
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());
        card.add(UITheme.scroll(table), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void refreshSales() {
        salesModel.setRowCount(0);
        LocalDate from = FormSupport.spinnerDate(fromSpinner);
        LocalDate to = FormSupport.spinnerDate(toSpinner);
        try {
            List<Sale> sales = saleDAO.findByDateRange(from, to);
            for (Sale sale : sales) {
                salesModel.addRow(new Object[]{
                        sale.getSaleId(),
                        sale.getSaleDate() == null ? "" : DATE.format(sale.getSaleDate()),
                        sale.getCashierName(),
                        sale.getItems().size(),
                        CurrencyUtil.format(sale.getTotalAmount())
                });
            }
            salesSummary.setText(sales.size() + " sales  ·  "
                    + CurrencyUtil.format(saleDAO.totalSalesBetween(from, to)));
        } catch (SQLException ex) {
            FormSupport.error(this, "Could not load sales report: " + ex.getMessage());
        }
    }

    private void refreshItemWise() {
        itemModel.setRowCount(0);
        LocalDate from = FormSupport.spinnerDate(fromSpinner);
        LocalDate to = FormSupport.spinnerDate(toSpinner);
        try {
            for (Object[] row : saleDAO.itemWiseReport(from, to)) {
                itemModel.addRow(new Object[]{
                        row[0], row[1], row[2], row[3], CurrencyUtil.format((java.math.BigDecimal) row[4])
                });
            }
        } catch (SQLException ex) {
            FormSupport.error(this, "Could not load item-wise report: " + ex.getMessage());
        }
    }

    private void refreshLowStock() {
        lowModel.setRowCount(0);
        try {
            for (Medicine medicine : medicineDAO.findLowStock()) {
                lowModel.addRow(new Object[]{
                        medicine.getMedicineId(),
                        medicine.getName(),
                        medicine.getQuantityInStock(),
                        medicine.getReorderLevel(),
                        medicine.getSupplierName()
                });
            }
        } catch (SQLException ex) {
            FormSupport.error(this, "Could not load low stock report: " + ex.getMessage());
        }
    }

    private void refreshExpiry() {
        expiryModel.setRowCount(0);
        try {
            for (Medicine medicine : medicineDAO.findExpiringWithinDays(30)) {
                expiryModel.addRow(new Object[]{
                        medicine.getMedicineId(),
                        medicine.getName(),
                        medicine.getMedicineType(),
                        medicine.getQuantityInStock(),
                        DAY.format(medicine.getExpiryDate()),
                        medicine.getSupplierName()
                });
            }
        } catch (SQLException ex) {
            FormSupport.error(this, "Could not load expiry report: " + ex.getMessage());
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
