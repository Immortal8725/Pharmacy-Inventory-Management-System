package com.healthfirst.pims.db;

import com.healthfirst.pims.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public final class SchemaInitializer {

    private SchemaInitializer() {
    }

    public static void initialize(Connection connection) throws SQLException {
        createTables(connection);
        seedIfEmpty(connection);
    }

    private static void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(50) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        role VARCHAR(20) NOT NULL,
                        full_name VARCHAR(100) NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS suppliers (
                        supplier_id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        contact_person VARCHAR(100),
                        phone VARCHAR(20),
                        email VARCHAR(100),
                        address TEXT
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS medicines (
                        medicine_id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(150) NOT NULL,
                        company VARCHAR(100),
                        medicine_type VARCHAR(50),
                        price DECIMAL(10,2) NOT NULL,
                        quantity_in_stock INT NOT NULL DEFAULT 0,
                        reorder_level INT NOT NULL DEFAULT 10,
                        expiry_date DATE NOT NULL,
                        supplier_id INT,
                        CONSTRAINT fk_medicine_supplier
                            FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS sales (
                        sale_id INT AUTO_INCREMENT PRIMARY KEY,
                        sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        total_amount DECIMAL(10,2) NOT NULL,
                        user_id INT NOT NULL,
                        CONSTRAINT fk_sale_user
                            FOREIGN KEY (user_id) REFERENCES users(user_id)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS sale_items (
                        sale_item_id INT AUTO_INCREMENT PRIMARY KEY,
                        sale_id INT NOT NULL,
                        medicine_id INT NOT NULL,
                        quantity_sold INT NOT NULL,
                        price_at_sale DECIMAL(10,2) NOT NULL,
                        CONSTRAINT fk_item_sale
                            FOREIGN KEY (sale_id) REFERENCES sales(sale_id),
                        CONSTRAINT fk_item_medicine
                            FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
                    )
                    """);
        }
    }

    private static void seedIfEmpty(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }
        seedUsers(connection);
        seedSuppliers(connection);
        seedMedicines(connection);
        seedSales(connection);
    }

    private static void seedUsers(Connection connection) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            insertUser(ps, "admin", "admin123", "Admin", "Thandiwe Mokoena");
            insertUser(ps, "cashier", "cash123", "Cashier", "Lebo Dlamini");
            insertUser(ps, "manager", "manager123", "Admin", "Sipho Ndlovu");
            insertUser(ps, "cashier2", "cash123", "Cashier", "Ayesha Patel");
        }
    }

    private static void insertUser(PreparedStatement ps, String username, String password,
                                   String role, String fullName) throws SQLException {
        ps.setString(1, username);
        ps.setString(2, PasswordUtil.hash(password));
        ps.setString(3, role);
        ps.setString(4, fullName);
        ps.executeUpdate();
    }

    private static void seedSuppliers(Connection connection) throws SQLException {
        String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            insertSupplier(ps, "PharmaPlus Distributors", "Naledi Khumalo", "011-555-0142",
                    "orders@pharmaplus.co.za", "14 Commissioner Street, Johannesburg, 2001");
            insertSupplier(ps, "MedSupply SA", "Johan van der Berg", "031-555-0198",
                    "sales@medsupply.co.za", "88 Umgeni Road, Durban, 4001");
            insertSupplier(ps, "Apex Pharmaceuticals", "Fatima Abrahams", "021-555-0166",
                    "support@apexpharma.co.za", "5 Bree Street, Cape Town, 8001");
            insertSupplier(ps, "Clicks Wholesale", "Priya Naidoo", "011-555-0201",
                    "wholesale@clicks.co.za", "Cnr Rivonia & Grayston, Sandton, 2196");
        }
    }

    private static void insertSupplier(PreparedStatement ps, String name, String contact,
                                       String phone, String email, String address) throws SQLException {
        ps.setString(1, name);
        ps.setString(2, contact);
        ps.setString(3, phone);
        ps.setString(4, email);
        ps.setString(5, address);
        ps.executeUpdate();
    }

    private static void seedMedicines(Connection connection) throws SQLException {
        String sql = """
                INSERT INTO medicines
                    (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            insertMedicine(ps, "Panado 500mg Tablets", "Adcock Ingram", "Tablet", 49.95, 120, 30, "2027-06-30", 1);
            insertMedicine(ps, "Disprin 300mg", "Reckitt", "Tablet", 32.50, 80, 20, "2027-03-15", 1);
            insertMedicine(ps, "Augmentin 625mg", "GSK", "Tablet", 189.00, 8, 15, "2026-10-05", 2);
            insertMedicine(ps, "Allergex 4mg", "Pharmacare", "Tablet", 28.90, 45, 20, "2027-11-01", 3);
            insertMedicine(ps, "Benylin Cough Syrup", "Johnson & Johnson", "Syrup", 89.95, 25, 10, "2026-10-12", 2);
            insertMedicine(ps, "Voltaren Emulgel", "Novartis", "Cream", 112.00, 18, 8, "2028-01-20", 3);
            insertMedicine(ps, "Insulin Actrapid", "Novo Nordisk", "Injection", 245.00, 6, 10, "2026-09-28", 2);
            insertMedicine(ps, "Grand-Pa Headache Powder", "GSK", "Powder", 15.50, 200, 50, "2027-08-01", 4);
            insertMedicine(ps, "Nurofen 200mg", "Reckitt", "Capsule", 64.90, 40, 15, "2027-04-22", 1);
            insertMedicine(ps, "Betadine Cream", "Mundipharma", "Cream", 54.00, 22, 10, "2027-12-31", 4);
            insertMedicine(ps, "Amoxicillin 250mg", "Aspen", "Capsule", 75.00, 12, 20, "2027-02-14", 3);
            insertMedicine(ps, "Corenza C", "Adcock Ingram", "Tablet", 48.00, 60, 20, "2026-10-20", 1);
            insertMedicine(ps, "Gaviscon Liquid", "Reckitt", "Syrup", 95.50, 14, 8, "2027-07-07", 4);
            insertMedicine(ps, "Ventolin Inhaler", "GSK", "Inhaler", 165.00, 9, 12, "2027-09-01", 2);
            insertMedicine(ps, "Calpol Paediatric Syrup", "GSK", "Syrup", 72.00, 30, 10, "2027-05-18", 2);
            insertMedicine(ps, "Prednisone 5mg", "Aspen", "Tablet", 38.00, 50, 15, "2028-03-03", 3);
            insertMedicine(ps, "Citro-Soda", "Adcock Ingram", "Powder", 42.90, 35, 10, "2027-01-10", 1);
            insertMedicine(ps, "Deep Heat Rub", "Mentholatum", "Cream", 36.50, 28, 10, "2028-06-15", 4);
        }
    }

    private static void insertMedicine(PreparedStatement ps, String name, String company, String type,
                                       double price, int qty, int reorder, String expiry,
                                       int supplierId) throws SQLException {
        ps.setString(1, name);
        ps.setString(2, company);
        ps.setString(3, type);
        ps.setBigDecimal(4, java.math.BigDecimal.valueOf(price).setScale(2, java.math.RoundingMode.HALF_UP));
        ps.setInt(5, qty);
        ps.setInt(6, reorder);
        ps.setDate(7, java.sql.Date.valueOf(LocalDate.parse(expiry)));
        ps.setInt(8, supplierId);
        ps.executeUpdate();
    }

    private static void seedSales(Connection connection) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            createSale(connection, 2, "2026-09-01 09:15:00",
                    new int[]{1, 8, 4}, new int[]{2, 3, 1}, new double[]{49.95, 15.50, 28.90});
            createSale(connection, 2, "2026-09-03 14:40:00",
                    new int[]{12, 5, 9}, new int[]{1, 1, 2}, new double[]{48.00, 89.95, 64.90});
            createSale(connection, 4, "2026-09-05 11:05:00",
                    new int[]{2, 17, 10}, new int[]{2, 1, 1}, new double[]{32.50, 42.90, 54.00});
            createSale(connection, 2, "2026-09-08 16:22:00",
                    new int[]{15, 8}, new int[]{1, 4}, new double[]{72.00, 15.50});
            createSale(connection, 4, "2026-09-10 10:08:00",
                    new int[]{6, 13}, new int[]{1, 1}, new double[]{112.00, 95.50});
            createSale(connection, 2, "2026-09-12 13:33:00",
                    new int[]{1, 9, 16, 18}, new int[]{3, 1, 1, 1}, new double[]{49.95, 64.90, 38.00, 36.50});
            createSale(connection, 2, "2026-09-14 08:50:00",
                    new int[]{11, 3}, new int[]{1, 1}, new double[]{75.00, 189.00});
            createSale(connection, 4, "2026-09-15 09:05:00",
                    new int[]{1, 12, 8}, new int[]{1, 2, 2}, new double[]{49.95, 48.00, 15.50});
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private static void createSale(Connection connection, int userId, String timestamp,
                                   int[] medicineIds, int[] quantities, double[] prices) throws SQLException {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (int i = 0; i < quantities.length; i++) {
            total = total.add(java.math.BigDecimal.valueOf(prices[i])
                    .multiply(java.math.BigDecimal.valueOf(quantities[i])));
        }
        total = total.setScale(2, java.math.RoundingMode.HALF_UP);

        int saleId;
        String saleSql = "INSERT INTO sales (sale_date, total_amount, user_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(timestamp));
            ps.setBigDecimal(2, total);
            ps.setInt(3, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to create sample sale");
                }
                saleId = keys.getInt(1);
            }
        }

        String itemSql = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
        String stockSql = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ?";
        try (PreparedStatement itemPs = connection.prepareStatement(itemSql);
             PreparedStatement stockPs = connection.prepareStatement(stockSql)) {
            for (int i = 0; i < medicineIds.length; i++) {
                itemPs.setInt(1, saleId);
                itemPs.setInt(2, medicineIds[i]);
                itemPs.setInt(3, quantities[i]);
                itemPs.setBigDecimal(4, java.math.BigDecimal.valueOf(prices[i]).setScale(2, java.math.RoundingMode.HALF_UP));
                itemPs.executeUpdate();

                stockPs.setInt(1, quantities[i]);
                stockPs.setInt(2, medicineIds[i]);
                stockPs.executeUpdate();
            }
        }
    }
}
