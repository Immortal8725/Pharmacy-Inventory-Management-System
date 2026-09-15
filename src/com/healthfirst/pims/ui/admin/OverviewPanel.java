package com.healthfirst.pims.ui.admin;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.dao.SaleDAO;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.ui.components.StatCard;
import com.healthfirst.pims.ui.theme.UITheme;
import com.healthfirst.pims.util.CurrencyUtil;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OverviewPanel extends JPanel {

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final StatCard stockCard = new StatCard("Medicines on file", "—", "Active SKUs", UITheme.PRIMARY);
    private final StatCard lowCard = new StatCard("Low stock", "—", "At or below reorder level", UITheme.DANGER);
    private final StatCard expiryCard = new StatCard("Expiring soon", "—", "Next 30 days", UITheme.WARNING);
    private final StatCard salesCard = new StatCard("Sales this month", "—", "Completed transactions", UITheme.SUCCESS);
    private final DefaultTableModel alertModel = new DefaultTableModel(
            new String[]{"Alert", "Medicine", "Detail"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public OverviewPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(UITheme.heading("Operations overview"), BorderLayout.NORTH);
        top.add(UITheme.muted("Live inventory health and trading for HealthFirst Pharmacy."), BorderLayout.SOUTH);

        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 12));
        stats.setOpaque(false);
        stats.setPreferredSize(new java.awt.Dimension(10, 118));
        stats.add(stockCard);
        stats.add(lowCard);
        stats.add(expiryCard);
        stats.add(salesCard);

        JPanel north = new JPanel(new BorderLayout(0, 12));
        north.setOpaque(false);
        north.add(top, BorderLayout.NORTH);
        north.add(stats, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        JPanel alerts = UITheme.card();
        alerts.setLayout(new BorderLayout(0, 10));
        JLabel title = new JLabel("Attention required");
        title.setFont(UITheme.SUBHEADING);
        alerts.add(title, BorderLayout.NORTH);
        JTable table = new JTable(alertModel);
        UITheme.styleTable(table);
        JScrollPane scroll = UITheme.scroll(table);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        alerts.add(scroll, BorderLayout.CENTER);
        add(alerts, BorderLayout.CENTER);
    }

    public void reload() {
        try {
            int medicines = medicineDAO.countAll();
            int low = medicineDAO.countLowStock();
            List<Medicine> expiring = medicineDAO.findExpiringWithinDays(30);
            LocalDate start = LocalDate.now().withDayOfMonth(1);
            int sales = saleDAO.countSalesBetween(start, LocalDate.now());
            String revenue = CurrencyUtil.format(saleDAO.totalSalesBetween(start, LocalDate.now()));

            stockCard.setValue(String.valueOf(medicines));
            lowCard.setValue(String.valueOf(low));
            expiryCard.setValue(String.valueOf(expiring.size()));
            salesCard.setValue(revenue);

            alertModel.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            for (Medicine medicine : medicineDAO.findLowStock()) {
                alertModel.addRow(new Object[]{
                        "Low stock",
                        medicine.getName(),
                        medicine.getQuantityInStock() + " left  ·  reorder at " + medicine.getReorderLevel()
                });
            }
            for (Medicine medicine : expiring) {
                alertModel.addRow(new Object[]{
                        "Expiring",
                        medicine.getName(),
                        "Expires " + medicine.getExpiryDate().format(fmt)
                });
            }
            if (alertModel.getRowCount() == 0) {
                alertModel.addRow(new Object[]{"All clear", "—", "No low-stock or near-expiry items"});
            }
        } catch (SQLException ex) {
            alertModel.setRowCount(0);
            alertModel.addRow(new Object[]{"Error", "Database", ex.getMessage()});
        }
    }
}
