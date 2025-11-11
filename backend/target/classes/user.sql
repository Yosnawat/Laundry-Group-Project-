-- Inserts users (No changes needed)
INSERT INTO users (student_id, name, email, password, role, is_active, created_at, updated_at)
VALUES 
('6731503032', 'Wacharaphong Sutthiboriban', '6731503032@lamduan.mfu.ac.th', 'password', 'STUDENT', TRUE, NOW(), NOW()),
('ADMIN', 'Manager', 'admin@example.com', 'adminpassword', 'MANAGER', TRUE, NOW(), NOW()),
('1', '1', '1@gmail.com', '1', 'MANAGER', TRUE, NOW(), NOW()),
('2', '2', '2@gmail.com', '1', 'STUDENT', TRUE, NOW(), NOW());
-- Inserts machines (No changes needed)
INSERT INTO machines (machine_number, name, machine_type, brand, model, capacity, status, location, description, price_per_hour, price_per_day, current_user_id, usage_start_time, created_at, updated_at) 
VALUES 
('001', 'Washing Machine - High Speed', 'Washing Machine', 'LG', 'WM-2000', '8kg', 'AVAILABLE', 'Laundry Room A', 'High-speed washing machine with eco mode', 2.50, 15.00, NULL, NULL, NOW(), NOW()),
('002', 'Dryer Machine - Standard', 'Dryer', 'Samsung', 'DRY-500', '6kg', 'AVAILABLE', 'Laundry Room A', 'Standard dryer machine with multiple heat settings', 1.50, 10.00, NULL, NULL, NOW(), NOW()),
('003', 'Washing Machine - Eco', 'Washing Machine', 'Electrolux', 'WM-E100', '7kg', 'AVAILABLE', 'Laundry Room B', 'Eco-friendly model', 2.00, 12.00, NULL, NULL, NOW(), NOW()),
('004', 'Washing Machine - Standard', 'Washing Machine', 'Samsung', 'WM-S700', '8kg', 'AVAILABLE', 'Laundry Room B', 'Standard workhorse machine', 2.50, 15.00, NULL, NULL, NOW(), NOW()),
('005', 'Washing Machine - Large', 'Washing Machine', 'LG', 'WM-L3000', '12kg', 'AVAILABLE', 'Laundry Room C', 'Large capacity for blankets', 3.50, 20.00, NULL, NULL, NOW(), NOW()),
('006', 'Washing Machine - Compact', 'Washing Machine', 'Toshiba', 'WM-C50', '5kg', 'AVAILABLE', 'Laundry Room C', 'Compact machine for small loads', 1.50, 10.00, NULL, NULL, NOW(), NOW());

--
-- (THIS SECTION IS FIXED)
-- We remove the 'id' column so the database will auto-generate it.
--

-- Booking 1 (Will get ID=1)
INSERT INTO bookings (user_id, machine_id, booking_date, confirmation_expiry_time, rating, amount, service, created_at, updated_at)
VALUES 
(1, 1, '2025-10-28 10:00:00', NULL, NULL, 5.50, 'Washing', NOW(), NOW());

-- This still works, it refers to the booking with ID=1
INSERT INTO booking_status (booking_id, name, display_name, updated_at) 
VALUES (1, 'CONFIRMED', 'Confirmed', NOW());


-- Booking 2 (Will get ID=2)
INSERT INTO bookings (user_id, machine_id, booking_date, confirmation_expiry_time, rating, amount, service, created_at, updated_at)
VALUES 
(1, 2, '2025-10-28 14:00:00', NULL, NULL, 5.50, 'Washing', NOW(), NOW());

-- This still works, it refers to the booking with ID=2
INSERT INTO booking_status (booking_id, name, display_name, updated_at) 
VALUES (2, 'PENDING', 'Pending', NOW());


-- Booking 3 (Will get ID=3)
INSERT INTO bookings (user_id, machine_id, booking_date, confirmation_expiry_time, rating, amount, service, created_at, updated_at)
VALUES 
(2, 1, '2025-10-29 09:00:00', NULL, NULL, 5.50, 'Washing', NOW(), NOW());

-- This still works, it refers to the booking with ID=3
INSERT INTO booking_status (booking_id, name, display_name, updated_at) 
VALUES (3, 'CONFIRMED', 'Confirmed', NOW());
