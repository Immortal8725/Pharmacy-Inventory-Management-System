package com.healthfirst.pims.model;

public class User {

    private int userId;
    private String username;
    private String passwordHash;
    private String role;
    private String fullName;

    public User() {
    }

    public User(int userId, String username, String role, String fullName) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }

    public boolean isCashier() {
        return "Cashier".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
