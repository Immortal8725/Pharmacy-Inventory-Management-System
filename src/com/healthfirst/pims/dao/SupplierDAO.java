package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DatabaseManager;
import com.healthfirst.pims.model.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public List<Supplier> findAll() throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT supplier_id, name, contact_person, phone, email, address FROM suppliers ORDER BY name";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                suppliers.add(mapSupplier(rs));
            }
        }
        return suppliers;
    }

    public Supplier findById(int supplierId) throws SQLException {
        String sql = "SELECT supplier_id, name, contact_person, phone, email, address FROM suppliers WHERE supplier_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapSupplier(rs) : null;
            }
        }
    }

    public void insert(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, supplier);
            ps.executeUpdate();
        }
    }

    public void update(Supplier supplier) throws SQLException {
        String sql = """
                UPDATE suppliers
                SET name = ?, contact_person = ?, phone = ?, email = ?, address = ?
                WHERE supplier_id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, supplier);
            ps.setInt(6, supplier.getSupplierId());
            ps.executeUpdate();
        }
    }

    public void delete(int supplierId) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Supplier supplier) throws SQLException {
        ps.setString(1, supplier.getName());
        ps.setString(2, supplier.getContactPerson());
        ps.setString(3, supplier.getPhone());
        ps.setString(4, supplier.getEmail());
        ps.setString(5, supplier.getAddress());
    }

    private Supplier mapSupplier(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setSupplierId(rs.getInt("supplier_id"));
        supplier.setName(rs.getString("name"));
        supplier.setContactPerson(rs.getString("contact_person"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setEmail(rs.getString("email"));
        supplier.setAddress(rs.getString("address"));
        return supplier;
    }
}
