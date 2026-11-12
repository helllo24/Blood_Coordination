-- ==============================================================================
-- Project: Emergency Blood Request and Donor Coordination System
-- Database: blood_donor_db (MySQL 8.0)
-- Architecture: MVC with Java Servlet, JSP, JDBC
-- ==============================================================================

DROP DATABASE IF EXISTS blood_donor_db;
CREATE DATABASE blood_donor_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blood_donor_db;

-- ------------------------------------------------------------------------------
-- 1. Table: users
-- Core authentication and user management table
-- Roles: ADMIN, DONOR, REQUESTER
-- Password stored using SHA-256 secure hashing
-- ------------------------------------------------------------------------------
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role ENUM('ADMIN', 'DONOR', 'REQUESTER') NOT NULL,
    status ENUM('ACTIVE', 'PENDING', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 2. Table: donors
-- Profile details for voluntary blood donors
-- Relationship: One user has one donor profile (users.id 1:1 donors.user_id)
-- ------------------------------------------------------------------------------
CREATE TABLE donors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    blood_group ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    age INT NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    address TEXT,
    last_donation_date DATE NULL,
    is_available BOOLEAN DEFAULT TRUE,
    total_donations INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_donor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_donor_blood (blood_group),
    INDEX idx_donor_city (city),
    INDEX idx_donor_available (is_available)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 3. Table: blood_requests
-- Emergency blood requests created by patients/requesters
-- Relationship: One requester can create multiple blood requests (users.id 1:N blood_requests.requester_id)
-- ------------------------------------------------------------------------------
CREATE TABLE blood_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    patient_name VARCHAR(150) NOT NULL,
    blood_group ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    units_needed INT NOT NULL DEFAULT 1,
    hospital_name VARCHAR(255) NOT NULL,
    hospital_city VARCHAR(100) NOT NULL,
    hospital_address TEXT,
    contact_person VARCHAR(150) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    urgency_level ENUM('NORMAL', 'URGENT', 'CRITICAL') DEFAULT 'URGENT',
    status ENUM('PENDING', 'MATCHED', 'FULFILLED', 'CANCELLED') DEFAULT 'PENDING',
    required_date DATE NOT NULL,
    additional_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_request_blood (blood_group),
    INDEX idx_request_city (hospital_city),
    INDEX idx_request_status (status),
    INDEX idx_request_urgency (urgency_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 4. Table: donor_responses
-- Responses submitted by voluntary donors to blood requests
-- Relationships:
-- - One blood request can have multiple donor responses (blood_requests.id 1:N donor_responses.request_id)
-- - One donor can respond to multiple requests (donors.id 1:N donor_responses.donor_id)
-- ------------------------------------------------------------------------------
CREATE TABLE donor_responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    status ENUM('ACCEPTED', 'REJECTED', 'PENDING', 'COMPLETED') DEFAULT 'ACCEPTED',
    message VARCHAR(255) NULL,
    responded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_response_request FOREIGN KEY (request_id) REFERENCES blood_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_response_donor FOREIGN KEY (donor_id) REFERENCES donors(id) ON DELETE CASCADE,
    INDEX idx_response_request (request_id),
    INDEX idx_response_donor (donor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 5. Table: donations
-- Official donation audit history
-- Relationship: One donor can have multiple donation records (donors.id 1:N donations.donor_id)
-- ------------------------------------------------------------------------------
CREATE TABLE donations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NOT NULL,
    request_id BIGINT NULL,
    units_donated INT NOT NULL DEFAULT 1,
    donation_date DATE NOT NULL,
    hospital_or_camp VARCHAR(255) NOT NULL,
    status ENUM('COMPLETED', 'SCHEDULED', 'CANCELLED') DEFAULT 'COMPLETED',
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_donation_donor FOREIGN KEY (donor_id) REFERENCES donors(id) ON DELETE CASCADE,
    CONSTRAINT fk_donation_request FOREIGN KEY (request_id) REFERENCES blood_requests(id) ON DELETE SET NULL,
    INDEX idx_donation_donor (donor_id),
    INDEX idx_donation_date (donation_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 6. Table: blood_compatibility
-- Lookup reference mapping donor blood groups to recipient blood groups
-- ------------------------------------------------------------------------------
CREATE TABLE blood_compatibility (
    donor_blood_group VARCHAR(5) NOT NULL,
    recipient_blood_group VARCHAR(5) NOT NULL,
    PRIMARY KEY (donor_blood_group, recipient_blood_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 7. Table: notifications
-- In-app notifications for emergency matching and status updates
-- Relationship: One user can receive multiple notifications (users.id 1:N notifications.user_id)
-- ------------------------------------------------------------------------------
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('EMERGENCY_REQUEST', 'DONOR_RESPONSE', 'STATUS_UPDATE', 'GENERAL') DEFAULT 'GENERAL',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_unread (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==============================================================================
-- Sample Data Insertion
-- Password Hashes generated with SHA-256:
-- Admin@123 -> e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7
-- Donor@123 -> bfcf2bbe9b087cada0c88fa2976ea6cacc4604e5b5e8dc27e10c12c6cfb435f5
-- Req@123   -> 911cadab72fc161915a470ac885fd64ea58b989a444b2a4cbe740035a91a4801
-- ==============================================================================

-- 1. Seed Users (Admin, Donors, Requesters)
INSERT INTO users (id, full_name, email, password, phone, role, status) VALUES
(1, 'System Administrator', 'admin@bloodconnect.org', 'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7', '+91 9876543210', 'ADMIN', 'ACTIVE'),
(2, 'Rahul Sharma', 'rahul.donor@gmail.com', 'bfcf2bbe9b087cada0c88fa2976ea6cacc4604e5b5e8dc27e10c12c6cfb435f5', '+91 9876500001', 'DONOR', 'ACTIVE'),
(3, 'Priya Patel', 'priya.donor@gmail.com', 'bfcf2bbe9b087cada0c88fa2976ea6cacc4604e5b5e8dc27e10c12c6cfb435f5', '+91 9876500002', 'DONOR', 'ACTIVE'),
(4, 'Ankit Verma', 'ankit.donor@gmail.com', 'bfcf2bbe9b087cada0c88fa2976ea6cacc4604e5b5e8dc27e10c12c6cfb435f5', '+91 9876500003', 'DONOR', 'ACTIVE'),
(5, 'Vikram Mehta', 'vikram.req@gmail.com', '911cadab72fc161915a470ac885fd64ea58b989a444b2a4cbe740035a91a4801', '+91 9876500004', 'REQUESTER', 'ACTIVE'),
(6, 'Deepa Nair', 'deepa.req@gmail.com', '911cadab72fc161915a470ac885fd64ea58b989a444b2a4cbe740035a91a4801', '+91 9876500005', 'REQUESTER', 'ACTIVE');

-- 2. Seed Donors (Linked 1:1 with users 2, 3, 4)
INSERT INTO donors (id, user_id, blood_group, age, gender, city, state, pincode, address, last_donation_date, is_available, total_donations) VALUES
(1, 2, 'O+', 26, 'MALE', 'Bangalore', 'Karnataka', '560001', '124, 1st Cross, Indiranagar', '2026-08-15', TRUE, 4),
(2, 3, 'A+', 24, 'FEMALE', 'Bangalore', 'Karnataka', '560034', '58, Koramangala 4th Block', '2026-07-20', TRUE, 2),
(3, 4, 'B+', 29, 'MALE', 'Bangalore', 'Karnataka', '560068', '402, Green Glen Layout, Bellandur', '2026-09-10', TRUE, 5);

-- 3. Seed Blood Requests (Created by Requesters 5 and 6)
INSERT INTO blood_requests (id, requester_id, patient_name, blood_group, units_needed, hospital_name, hospital_city, hospital_address, contact_person, contact_phone, urgency_level, status, required_date, additional_notes) VALUES
(1, 5, 'Suresh Mehta', 'O+', 2, 'City Care Multi-Speciality Hospital', 'Bangalore', 'Bannerghatta Road, Bangalore', 'Vikram Mehta (Son)', '+91 9876500004', 'CRITICAL', 'PENDING', '2026-11-15', 'Urgent cardiac surgery replacement requirement.'),
(2, 6, 'Aarav Nair', 'A+', 1, 'Fortis Healthcare Hospital', 'Bangalore', 'Cunningham Road, Bangalore', 'Deepa Nair (Mother)', '+91 9876500005', 'URGENT', 'MATCHED', '2026-11-16', 'Pediatric surgery scheduled.');

-- 4. Seed Donor Responses (Donors responding to requests)
INSERT INTO donor_responses (id, request_id, donor_id, status, message) VALUES
(1, 1, 1, 'ACCEPTED', 'I am available to donate O+ blood tomorrow morning at City Care Hospital.'),
(2, 2, 2, 'ACCEPTED', 'Can reach Fortis Hospital by 11:00 AM.');

-- 5. Seed Donation History
INSERT INTO donations (id, donor_id, request_id, units_donated, donation_date, hospital_or_camp, status, remarks) VALUES
(1, 1, NULL, 1, '2026-08-15', 'Red Cross Voluntary Blood Donation Camp, Bangalore', 'COMPLETED', 'Regular voluntary donation.'),
(2, 2, NULL, 1, '2026-07-20', 'Manipal Hospital Blood Bank', 'COMPLETED', 'Platelet donation.'),
(3, 3, NULL, 1, '2026-09-10', 'Rotary Blood Bank, Bangalore', 'COMPLETED', 'Replacement donation.');

-- 6. Seed Blood Compatibility (Clinical Lookup Matrix)
INSERT INTO blood_compatibility (donor_blood_group, recipient_blood_group) VALUES
('O-', 'O-'), ('O-', 'O+'), ('O-', 'A-'), ('O-', 'A+'), ('O-', 'B-'), ('O-', 'B+'), ('O-', 'AB-'), ('O-', 'AB+'),
('O+', 'O+'), ('O+', 'A+'), ('O+', 'B+'), ('O+', 'AB+'),
('A-', 'A-'), ('A-', 'A+'), ('A-', 'AB-'), ('A-', 'AB+'),
('A+', 'A+'), ('A+', 'AB+'),
('B-', 'B-'), ('B-', 'B+'), ('B-', 'AB-'), ('B-', 'AB+'),
('B+', 'B+'), ('B+', 'AB+'),
('AB-', 'AB-'), ('AB-', 'AB+'),
('AB+', 'AB+');

-- 7. Seed Notifications
INSERT INTO notifications (id, user_id, title, message, type, is_read) VALUES
(1, 2, 'Emergency Blood Match in Bangalore', 'Urgent requirement for O+ blood at City Care Multi-Speciality Hospital.', 'EMERGENCY_REQUEST', FALSE),
(2, 5, 'Donor Responded to Your Request', 'Rahul Sharma (O+) has accepted your blood request for Suresh Mehta.', 'DONOR_RESPONSE', FALSE),
(3, 1, 'System Ready', 'Emergency Blood Request and Donor Coordination System database initialized successfully.', 'GENERAL', TRUE);
