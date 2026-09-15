package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DatabaseManager;
import com.healthfirst.pims.model.CartItem;
import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.model.SaleItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SaleDAO {

    public Sale checkout(int userId, List<CartItem> cart) throws SQLException {
        if (cart == null || cart.isEmpty()) {
            throw new SQLException("Cart is empty");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            total = total.add(item.getLineTotal());
        }
        total = total.setScale(2, java.math.RoundingMode.HALF_UP);

        try (Connection connection = DatabaseManager.getConnection()) {
            boolean original = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                for (CartItem item : cart) {
                    int available = currentStock(connection, item.getMedicine().getMedicineId());
                    if (item.getQuantity() > available) {
                        throw new SQLException("Insufficient stock for " + item.getMedicine().getName()
                                + " (available: " + available + ")");
                    }
                }

                int saleId;
                String saleSql = "INSERT INTO sales (sale_date, total_amount, user_id) VALUES (?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setBigDecimal(2, total);
                    ps.setInt(3, userId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Unable to create sale");
                        }
                        saleId = keys.getInt(1);
                    }
                }

                String itemSql = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
                String stockSql = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ?";
                try (PreparedStatement itemPs = connection.prepareStatement(itemSql);
                     PreparedStatement stockPs = connection.prepareStatement(stockSql)) {
                    for (CartItem item : cart) {
                        itemPs.setInt(1, saleId);
                        itemPs.setInt(2, item.getMedicine().getMedicineId());
                        itemPs.setInt(3, item.getQuantity());
                        itemPs.setBigDecimal(4, item.getUnitPrice());
                        itemPs.executeUpdate();

                        stockPs.setInt(1, item.getQuantity());
                        stockPs.setInt(2, item.getMedicine().getMedicineId());
                        stockPs.executeUpdate();
                    }
                }

                connection.commit();
                return findById(connection, saleId);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(original);
            }
        }
    }

    public Sale findById(int saleId) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            return findById(connection, saleId);
        }
    }

    private Sale findById(Connection connection, int saleId) throws SQLException {
        String sql = """
                SELECT s.sale_id, s.sale_date, s.total_amount, s.user_id, u.full_name
                FROM sales s
                JOIN users u ON u.user_id = s.user_id
                WHERE s.sale_id = ?
                """;
        Sale sale;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                sale = mapSale(rs);
            }
        }
        loadItems(connection, sale);
        return sale;
    }

    public List<Sale> findByDateRange(LocalDate from, LocalDate to) throws SQLException {
        Map<Integer, Sale> sales = new LinkedHashMap<>();
        String sql = """
                SELECT s.sale_id, s.sale_date, s.total_amount, s.user_id, u.full_name
                FROM sales s
                JOIN users u ON u.user_id = s.user_id
                WHERE CAST(s.sale_date AS DATE) BETWEEN ? AND ?
                ORDER BY s.sale_date DESC
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sale sale = mapSale(rs);
                    sales.put(sale.getSaleId(), sale);
                }
            }
            for (Sale sale : sales.values()) {
                loadItems(connection, sale);
            }
        }
        return new ArrayList<>(sales.values());
    }

    public List<Object[]> itemWiseReport(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT m.medicine_id, m.name, m.medicine_type,
                       COALESCE(SUM(si.quantity_sold), 0) AS units_sold,
                       COALESCE(SUM(si.quantity_sold * si.price_at_sale), 0) AS revenue
                FROM sale_items si
                JOIN sales s ON s.sale_id = si.sale_id
                JOIN medicines m ON m.medicine_id = si.medicine_id
                WHERE CAST(s.sale_date AS DATE) BETWEEN ? AND ?
                GROUP BY m.medicine_id, m.name, m.medicine_type
                ORDER BY units_sold DESC, m.name
                """;
        List<Object[]> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("medicine_id"),
                            rs.getString("name"),
                            rs.getString("medicine_type"),
                            rs.getInt("units_sold"),
                            rs.getBigDecimal("revenue")
                    });
                }
            }
        }
        return rows;
    }

    public BigDecimal totalSalesBetween(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(total_amount), 0)
                FROM sales
                WHERE CAST(sale_date AS DATE) BETWEEN ? AND ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    public int countSalesBetween(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM sales
                WHERE CAST(sale_date AS DATE) BETWEEN ? AND ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int currentStock(Connection connection, int medicineId) throws SQLException {
        String sql = "SELECT quantity_in_stock FROM medicines WHERE medicine_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Medicine not found");
                }
                return rs.getInt(1);
            }
        }
    }

    private void loadItems(Connection connection, Sale sale) throws SQLException {
        String sql = """
                SELECT si.sale_item_id, si.sale_id, si.medicine_id, m.name AS medicine_name,
                       si.quantity_sold, si.price_at_sale
                FROM sale_items si
                JOIN medicines m ON m.medicine_id = si.medicine_id
                WHERE si.sale_id = ?
                ORDER BY si.sale_item_id
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, sale.getSaleId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();
                    item.setSaleItemId(rs.getInt("sale_item_id"));
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setMedicineId(rs.getInt("medicine_id"));
                    item.setMedicineName(rs.getString("medicine_name"));
                    item.setQuantitySold(rs.getInt("quantity_sold"));
                    item.setPriceAtSale(rs.getBigDecimal("price_at_sale"));
                    sale.getItems().add(item);
                }
            }
        }
    }

    private Sale mapSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setSaleId(rs.getInt("sale_id"));
        Timestamp ts = rs.getTimestamp("sale_date");
        sale.setSaleDate(ts == null ? null : ts.toLocalDateTime());
        sale.setTotalAmount(rs.getBigDecimal("total_amount"));
        sale.setUserId(rs.getInt("user_id"));
        sale.setCashierName(rs.getString("full_name"));
        return sale;
    }
}
