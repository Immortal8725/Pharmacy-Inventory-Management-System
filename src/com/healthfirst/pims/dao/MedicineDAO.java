package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DatabaseManager;
import com.healthfirst.pims.model.Medicine;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    private static final String BASE_SELECT = """
            SELECT m.medicine_id, m.name, m.company, m.medicine_type, m.price,
                   m.quantity_in_stock, m.reorder_level, m.expiry_date, m.supplier_id,
                   s.name AS supplier_name
            FROM medicines m
            LEFT JOIN suppliers s ON s.supplier_id = m.supplier_id
            """;

    public List<Medicine> findAll() throws SQLException {
        return query(BASE_SELECT + " ORDER BY m.name");
    }

    public List<Medicine> search(String term) throws SQLException {
        String sql = BASE_SELECT + """
                WHERE LOWER(m.name) LIKE ? OR LOWER(m.company) LIKE ?
                   OR LOWER(m.medicine_type) LIKE ? OR CAST(m.medicine_id AS CHAR) = ?
                ORDER BY m.name
                """;
        String like = "%" + term.toLowerCase() + "%";
        List<Medicine> medicines = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    medicines.add(mapMedicine(rs));
                }
            }
        }
        return medicines;
    }

    public Medicine findById(int medicineId) throws SQLException {
        String sql = BASE_SELECT + " WHERE m.medicine_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapMedicine(rs) : null;
            }
        }
    }

    public List<Medicine> findLowStock() throws SQLException {
        return query(BASE_SELECT + " WHERE m.quantity_in_stock <= m.reorder_level ORDER BY m.quantity_in_stock");
    }

    public List<Medicine> findExpiringSoon() throws SQLException {
        return findExpiringWithinDays(30);
    }

    public List<Medicine> findExpiringWithinDays(int days) throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(days);
        String sql = BASE_SELECT + " WHERE m.expiry_date >= ? AND m.expiry_date <= ? ORDER BY m.expiry_date";
        List<Medicine> medicines = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(today));
            ps.setDate(2, Date.valueOf(until));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    medicines.add(mapMedicine(rs));
                }
            }
        }
        return medicines;
    }

    public void insert(Medicine medicine) throws SQLException {
        String sql = """
                INSERT INTO medicines
                    (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, medicine);
            ps.executeUpdate();
        }
    }

    public void update(Medicine medicine) throws SQLException {
        String sql = """
                UPDATE medicines
                SET name = ?, company = ?, medicine_type = ?, price = ?, quantity_in_stock = ?,
                    reorder_level = ?, expiry_date = ?, supplier_id = ?
                WHERE medicine_id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, medicine);
            ps.setInt(9, medicine.getMedicineId());
            ps.executeUpdate();
        }
    }

    public void delete(int medicineId) throws SQLException {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.executeUpdate();
        }
    }

    public int countAll() throws SQLException {
        return scalar("SELECT COUNT(*) FROM medicines");
    }

    public int countLowStock() throws SQLException {
        return scalar("SELECT COUNT(*) FROM medicines WHERE quantity_in_stock <= reorder_level");
    }

    private int scalar(String sql) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void bind(PreparedStatement ps, Medicine medicine) throws SQLException {
        ps.setString(1, medicine.getName());
        ps.setString(2, medicine.getCompany());
        ps.setString(3, medicine.getMedicineType());
        ps.setBigDecimal(4, medicine.getPrice());
        ps.setInt(5, medicine.getQuantityInStock());
        ps.setInt(6, medicine.getReorderLevel());
        ps.setDate(7, Date.valueOf(medicine.getExpiryDate()));
        ps.setInt(8, medicine.getSupplierId());
    }

    private List<Medicine> query(String sql) throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                medicines.add(mapMedicine(rs));
            }
        }
        return medicines;
    }

    private Medicine mapMedicine(ResultSet rs) throws SQLException {
        Medicine medicine = new Medicine();
        medicine.setMedicineId(rs.getInt("medicine_id"));
        medicine.setName(rs.getString("name"));
        medicine.setCompany(rs.getString("company"));
        medicine.setMedicineType(rs.getString("medicine_type"));
        medicine.setPrice(rs.getBigDecimal("price"));
        medicine.setQuantityInStock(rs.getInt("quantity_in_stock"));
        medicine.setReorderLevel(rs.getInt("reorder_level"));
        Date expiry = rs.getDate("expiry_date");
        medicine.setExpiryDate(expiry == null ? null : expiry.toLocalDate());
        medicine.setSupplierId(rs.getInt("supplier_id"));
        medicine.setSupplierName(rs.getString("supplier_name"));
        return medicine;
    }
}
