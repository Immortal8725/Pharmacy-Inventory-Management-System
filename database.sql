-- HealthFirst Pharmacy Inventory Management System
-- MySQL 8 schema, constraints, and sample data
-- Run this script as a MySQL administrator before using db.mode=mysql

CREATE DATABASE IF NOT EXISTS healthfirst_pims
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE healthfirst_pims;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS sale_items;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS medicines;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Cashier') NOT NULL,
    full_name VARCHAR(100) NOT NULL
);

CREATE TABLE suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT
);

CREATE TABLE medicines (
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
);

CREATE TABLE sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT fk_sale_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE sale_items (
    sale_item_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity_sold INT NOT NULL,
    price_at_sale DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_item_sale
        FOREIGN KEY (sale_id) REFERENCES sales(sale_id),
    CONSTRAINT fk_item_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
);

-- Passwords are stored as SHA-256 hashes
-- admin / admin123, cashier / cash123, manager / manager123, cashier2 / cash123
INSERT INTO users (username, password, role, full_name) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Admin', 'Thandiwe Mokoena'),
('cashier', 'c246650737293ddc18fc357393db78d1ecc9d1fd1af95469115e4a29f983359a', 'Cashier', 'Lebo Dlamini'),
('manager', '866485796cfa8d7c0cf7111640205b83076433547577511d81f8030ae99ecea5', 'Admin', 'Sipho Ndlovu'),
('cashier2', 'c246650737293ddc18fc357393db78d1ecc9d1fd1af95469115e4a29f983359a', 'Cashier', 'Ayesha Patel');

INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
('PharmaPlus Distributors', 'Naledi Khumalo', '011-555-0142', 'orders@pharmaplus.co.za', '14 Commissioner Street, Johannesburg, 2001'),
('MedSupply SA', 'Johan van der Berg', '031-555-0198', 'sales@medsupply.co.za', '88 Umgeni Road, Durban, 4001'),
('Apex Pharmaceuticals', 'Fatima Abrahams', '021-555-0166', 'support@apexpharma.co.za', '5 Bree Street, Cape Town, 8001'),
('Clicks Wholesale', 'Priya Naidoo', '011-555-0201', 'wholesale@clicks.co.za', 'Cnr Rivonia & Grayston, Sandton, 2196');

INSERT INTO medicines
    (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id)
VALUES
('Panado 500mg Tablets', 'Adcock Ingram', 'Tablet', 49.95, 120, 30, '2027-06-30', 1),
('Disprin 300mg', 'Reckitt', 'Tablet', 32.50, 80, 20, '2027-03-15', 1),
('Augmentin 625mg', 'GSK', 'Tablet', 189.00, 8, 15, '2026-10-05', 2),
('Allergex 4mg', 'Pharmacare', 'Tablet', 28.90, 45, 20, '2027-11-01', 3),
('Benylin Cough Syrup', 'Johnson & Johnson', 'Syrup', 89.95, 25, 10, '2026-10-12', 2),
('Voltaren Emulgel', 'Novartis', 'Cream', 112.00, 18, 8, '2028-01-20', 3),
('Insulin Actrapid', 'Novo Nordisk', 'Injection', 245.00, 6, 10, '2026-09-28', 2),
('Grand-Pa Headache Powder', 'GSK', 'Powder', 15.50, 200, 50, '2027-08-01', 4),
('Nurofen 200mg', 'Reckitt', 'Capsule', 64.90, 40, 15, '2027-04-22', 1),
('Betadine Cream', 'Mundipharma', 'Cream', 54.00, 22, 10, '2027-12-31', 4),
('Amoxicillin 250mg', 'Aspen', 'Capsule', 75.00, 12, 20, '2027-02-14', 3),
('Corenza C', 'Adcock Ingram', 'Tablet', 48.00, 60, 20, '2026-10-20', 1),
('Gaviscon Liquid', 'Reckitt', 'Syrup', 95.50, 14, 8, '2027-07-07', 4),
('Ventolin Inhaler', 'GSK', 'Inhaler', 165.00, 9, 12, '2027-09-01', 2),
('Calpol Paediatric Syrup', 'GSK', 'Syrup', 72.00, 30, 10, '2027-05-18', 2),
('Prednisone 5mg', 'Aspen', 'Tablet', 38.00, 50, 15, '2028-03-03', 3),
('Citro-Soda', 'Adcock Ingram', 'Powder', 42.90, 35, 10, '2027-01-10', 1),
('Deep Heat Rub', 'Mentholatum', 'Cream', 36.50, 28, 10, '2028-06-15', 4);

INSERT INTO sales (sale_date, total_amount, user_id) VALUES
('2026-09-01 09:15:00', 175.30, 2),
('2026-09-03 14:40:00', 267.75, 2),
('2026-09-05 11:05:00', 161.90, 4),
('2026-09-08 16:22:00', 134.00, 2),
('2026-09-10 10:08:00', 207.50, 4),
('2026-09-12 13:33:00', 289.25, 2),
('2026-09-14 08:50:00', 264.00, 2),
('2026-09-15 09:05:00', 176.95, 4);

INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES
(1, 1, 2, 49.95), (1, 8, 3, 15.50), (1, 4, 1, 28.90),
(2, 12, 1, 48.00), (2, 5, 1, 89.95), (2, 9, 2, 64.90),
(3, 2, 2, 32.50), (3, 17, 1, 42.90), (3, 10, 1, 54.00),
(4, 15, 1, 72.00), (4, 8, 4, 15.50),
(5, 6, 1, 112.00), (5, 13, 1, 95.50),
(6, 1, 3, 49.95), (6, 9, 1, 64.90), (6, 16, 1, 38.00), (6, 18, 1, 36.50),
(7, 11, 1, 75.00), (7, 3, 1, 189.00),
(8, 1, 1, 49.95), (8, 12, 2, 48.00), (8, 8, 2, 15.50);

UPDATE medicines m
JOIN (
    SELECT medicine_id, SUM(quantity_sold) AS sold
    FROM sale_items
    GROUP BY medicine_id
) x ON x.medicine_id = m.medicine_id
SET m.quantity_in_stock = m.quantity_in_stock - x.sold;
