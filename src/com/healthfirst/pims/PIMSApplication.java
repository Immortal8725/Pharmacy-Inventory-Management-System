package com.healthfirst.pims;

import com.healthfirst.pims.db.DatabaseManager;
import com.healthfirst.pims.ui.LoginFrame;
import com.healthfirst.pims.ui.theme.UITheme;
import com.healthfirst.pims.util.ScreenshotTool;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.nio.file.Path;

public final class PIMSApplication {

    private PIMSApplication() {
    }

    public static void main(String[] args) {
        if (args.length > 0 && "--screenshots".equals(args[0])) {
            Path output = args.length > 1 ? Path.of(args[1]) : Path.of("screenshots");
            try {
                ScreenshotTool.captureAll(output);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
            return;
        }

        if (args.length > 0 && "--self-test".equals(args[0])) {
            runSelfTest();
            return;
        }

        UITheme.install();
        try {
            DatabaseManager.initialize();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "The database could not be started.\n" + ex.getMessage(),
                    "HealthFirst PIMS", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(LoginFrame::showLogin);
    }

    private static void runSelfTest() {
        try {
            DatabaseManager.initialize();
            var users = new com.healthfirst.pims.dao.UserDAO();
            var admin = users.authenticate("admin", "admin123");
            var cashier = users.authenticate("cashier", "cash123");
            var failed = users.authenticate("admin", "wrong-password");
            if (admin == null || !admin.isAdmin()) {
                throw new IllegalStateException("Admin login failed");
            }
            if (cashier == null || !cashier.isCashier()) {
                throw new IllegalStateException("Cashier login failed");
            }
            if (failed != null) {
                throw new IllegalStateException("Invalid password was accepted");
            }
            var medicines = new com.healthfirst.pims.dao.MedicineDAO().findAll();
            if (medicines.size() < 10) {
                throw new IllegalStateException("Expected sample medicines");
            }
            var low = new com.healthfirst.pims.dao.MedicineDAO().findLowStock();
            var expiring = new com.healthfirst.pims.dao.MedicineDAO().findExpiringWithinDays(30);
            var sales = new com.healthfirst.pims.dao.SaleDAO()
                    .findByDateRange(java.time.LocalDate.of(2026, 9, 1), java.time.LocalDate.of(2026, 9, 15));
            if (sales.isEmpty()) {
                throw new IllegalStateException("Expected sample sales");
            }
            System.out.println("Self-test passed");
            System.out.println("  admin=" + admin.getFullName());
            System.out.println("  cashier=" + cashier.getFullName());
            System.out.println("  medicines=" + medicines.size());
            System.out.println("  lowStock=" + low.size());
            System.out.println("  expiring30d=" + expiring.size());
            System.out.println("  salesInRange=" + sales.size());
            System.out.println("  db=" + DatabaseManager.getActiveMode());
        } catch (Exception ex) {
            System.err.println("Self-test failed: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
