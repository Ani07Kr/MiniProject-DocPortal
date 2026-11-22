-- =======================================================
-- DocPortal Database Schema & Seed Data
-- Compatible with MySQL 8.0+, TiDB Cloud, Aiven, etc.
-- =======================================================

-- Create Database if creating on a self-hosted / local MySQL server
-- (In managed cloud databases like TiDB or Aiven, database is usually pre-created, e.g., 'test' or 'defaultdb')
CREATE DATABASE IF NOT EXISTS DocPortal;
USE DocPortal;

-- 1. User Authentication Table
CREATE TABLE IF NOT EXISTS loginone (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'APPROVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Doctors Table
CREATE TABLE IF NOT EXISTS doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    contact VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Appointments Table
CREATE TABLE IF NOT EXISTS appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    appointmentId VARCHAR(50) NOT NULL UNIQUE,
    doctorName VARCHAR(100) NOT NULL,
    patientId VARCHAR(50) NOT NULL,
    patientName VARCHAR(100) NOT NULL,
    appointmentDate DATE NOT NULL,
    time_slot VARCHAR(50) NULL,
    token_number INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Prescriptions Table
CREATE TABLE IF NOT EXISTS prescriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doctorName VARCHAR(100) NOT NULL,
    patientId VARCHAR(50) NOT NULL,
    prescription TEXT NOT NULL,
    valid_until DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =======================================================
-- Sample Seed Data (For testing right after setup)
-- =======================================================

-- Sample Users (Login credentials)
INSERT INTO loginone (username, email, password, role) VALUES 
('admin', 'admin@docportal.com', 'admin123', 'Admin'),
('dr_sharma', 'dr.sharma@docportal.com', 'doctor123', 'Doctor'),
('patient_rahul', 'rahul@gmail.com', 'patient123', 'Patient')
ON DUPLICATE KEY UPDATE username=username;

-- Sample Doctors
INSERT INTO doctors (name, specialty, contact) VALUES
('Dr. A. K. Sharma', 'Cardiology', '+91-9876543210'),
('Dr. Priya Verma', 'Dermatology', '+91-9876543211'),
('Dr. Rajesh Mehta', 'Orthopedics', '+91-9876543212');

-- Sample Appointments
INSERT INTO appointments (appointmentId, doctorName, patientId, patientName, appointmentDate) VALUES
('APT-1001', 'Dr. A. K. Sharma', 'P-501', 'Rahul Kumar', '2026-12-01'),
('APT-1002', 'Dr. Priya Verma', 'P-502', 'Sneha Patel', '2026-12-05')
ON DUPLICATE KEY UPDATE appointmentId=appointmentId;

-- Sample Prescriptions
INSERT INTO prescriptions (doctorName, patientId, prescription) VALUES
('Dr. A. K. Sharma', 'P-501', 'Tab Atorvastatin 10mg once daily after dinner for 30 days. Maintain low-sodium diet.');
