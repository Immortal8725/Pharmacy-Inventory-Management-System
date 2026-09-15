package com.healthfirst.pims.util;

import com.healthfirst.pims.dao.SaleDAO;
import com.healthfirst.pims.dao.UserDAO;
import com.healthfirst.pims.db.DatabaseManager;
import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.model.User;
import com.healthfirst.pims.ui.LoginFrame;
import com.healthfirst.pims.ui.admin.AdminDashboard;
import com.healthfirst.pims.ui.admin.ReportsPanel;
import com.healthfirst.pims.ui.cashier.BillDialog;
import com.healthfirst.pims.ui.cashier.CashierDashboard;
import com.healthfirst.pims.ui.theme.UITheme;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public final class ScreenshotTool {

    private ScreenshotTool() {
    }

    public static void captureAll(Path outputDir) throws Exception {
        UITheme.install();
        DatabaseManager.initialize();
        Files.createDirectories(outputDir);

        CountDownLatch done = new CountDownLatch(1);
        Exception[] failure = new Exception[1];
        SwingUtilities.invokeLater(() -> {
            try {
                run(outputDir);
            } catch (Exception ex) {
                failure[0] = ex;
            } finally {
                done.countDown();
            }
        });
        done.await();
        if (failure[0] != null) {
            throw failure[0];
        }
        System.out.println("Screenshots written to " + outputDir.toAbsolutePath());
        System.exit(0);
    }

    private static void run(Path outputDir) throws Exception {
        UserDAO users = new UserDAO();
        User admin = users.authenticate("admin", "admin123");
        User cashier = users.authenticate("cashier", "cash123");
        if (admin == null || cashier == null) {
            throw new IllegalStateException("Seed users are missing");
        }

        LoginFrame login = new LoginFrame();
        login.setVisible(true);
        pause();
        save(login, outputDir.resolve("01_login_screen.png"));
        login.dispose();

        Session.login(admin);
        AdminDashboard adminDash = new AdminDashboard();
        adminDash.setVisible(true);
        adminDash.showPage("overview");
        pause();
        save(adminDash, outputDir.resolve("02_admin_dashboard.png"));

        adminDash.showPage("medicines");
        pause();
        save(adminDash, outputDir.resolve("03_manage_medicines.png"));

        ReportsPanel reports = field(adminDash, "reportsPanel", ReportsPanel.class);
        adminDash.showPage("reports");
        reports.selectTab(0);
        pause();
        save(adminDash, outputDir.resolve("06_sales_report.png"));
        reports.selectTab(1);
        pause();
        save(adminDash, outputDir.resolve("07_item_wise_report.png"));
        reports.selectTab(2);
        pause();
        save(adminDash, outputDir.resolve("08_low_stock_report.png"));
        reports.selectTab(3);
        pause();
        save(adminDash, outputDir.resolve("09_expiry_report.png"));
        adminDash.dispose();

        Session.login(cashier);
        CashierDashboard cashierDash = new CashierDashboard();
        cashierDash.setVisible(true);
        cashierDash.getPosPanel().addDemoItems();
        pause();
        save(cashierDash, outputDir.resolve("04_cashier_pos.png"));

        Sale sale = new SaleDAO().findById(8);
        BillDialog bill = new BillDialog(cashierDash, sale, null);
        bill.setModal(false);
        bill.setVisible(true);
        pause();
        save(bill, outputDir.resolve("05_generated_bill.png"));
        bill.dispose();
        cashierDash.dispose();
    }

    private static <T> T field(Object target, String name, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }

    private static void pause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void save(java.awt.Window window, Path path) throws Exception {
        window.toFront();
        window.invalidate();
        window.validate();
        window.repaint();
        pause();
        BufferedImage image = capture(window);
        ImageIO.write(image, "png", path.toFile());
        System.out.println("Wrote " + path.getFileName());
    }

    private static BufferedImage capture(java.awt.Window window) {
        int width = Math.max(window.getWidth(), 1);
        int height = Math.max(window.getHeight(), 1);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, width, height);
        window.paintAll(g);
        g.dispose();
        return image;
    }
}
