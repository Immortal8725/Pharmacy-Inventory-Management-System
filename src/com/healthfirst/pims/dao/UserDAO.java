package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DatabaseManager;
import com.healthfirst.pims.model.User;
import com.healthfirst.pims.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, password, role, full_name FROM users WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String stored = rs.getString("password");
                if (!PasswordUtil.matches(password, stored)) {
                    return null;
                }
                return mapUser(rs);
            }
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password, role, full_name FROM users ORDER BY full_name";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }
        return users;
    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, password, role, full_name FROM users WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    public boolean usernameExists(String username, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM users WHERE username = ?"
                : "SELECT COUNT(*) FROM users WHERE username = ? AND user_id <> ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void insert(User user, String rawPassword) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtil.hash(rawPassword));
            ps.setString(3, user.getRole());
            ps.setString(4, user.getFullName());
            ps.executeUpdate();
        }
    }

    public void update(User user, String newRawPassword) throws SQLException {
        if (newRawPassword == null || newRawPassword.isBlank()) {
            String sql = "UPDATE users SET username = ?, role = ?, full_name = ? WHERE user_id = ?";
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getRole());
                ps.setString(3, user.getFullName());
                ps.setInt(4, user.getUserId());
                ps.executeUpdate();
            }
        } else {
            String sql = "UPDATE users SET username = ?, password = ?, role = ?, full_name = ? WHERE user_id = ?";
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, PasswordUtil.hash(newRawPassword));
                ps.setString(3, user.getRole());
                ps.setString(4, user.getFullName());
                ps.setInt(5, user.getUserId());
                ps.executeUpdate();
            }
        }
    }

    public void delete(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setFullName(rs.getString("full_name"));
        return user;
    }
}
