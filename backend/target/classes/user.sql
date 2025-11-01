INSERT INTO users (student_id, name, email, password, role, is_active, created_at, updated_at)
VALUES 
('6731503032', 'Wacharaphong Sutthiboriban', '6731503032@lamduan.mfu.ac.th', 'password', 'STUDENT', TRUE, NOW(), NOW());

INSERT INTO users (student_id, name, email, password, role, is_active, created_at, updated_at)
VALUES 
('ADMIN001', 'Manager', 'admin@example.com', 'adminpassword', 'MANAGER', TRUE, NOW(), NOW());

INSERT INTO machines (machine_number, name, machine_type, brand, model, capacity, status, location, description, price_per_hour, price_per_day, current_user_id, usage_start_time, created_at, updated_at) 
VALUES 
('001', 'Washing Machine - High Speed', 'Washing Machine', 'LG', 'WM-2000', '8kg', 'AVAILABLE', 'Laundry Room A', 'High-speed washing machine with eco mode', 2.50, 15.00, NULL, NULL, NOW(), NOW()),
('002', 'Dryer Machine - Standard', 'Dryer', 'Samsung', 'DRY-500', '6kg', 'AVAILABLE', 'Laundry Room A', 'Standard dryer machine with multiple heat settings', 1.50, 10.00, NULL, NULL, NOW(), NOW());

INSERT INTO bookings (user_id, machine_id, booking_date, status, amount, service, created_at, updated_at)
VALUES 
(1, 1, '2025-10-28 10:00:00', 'CONFIRMED', 5.50, 'Washing', NOW(), NOW()),
(1, 2, '2025-10-28 14:00:00', 'PENDING', 5.50, 'Washing', NOW(), NOW()),
(2, 1, '2025-10-29 09:00:00', 'CONFIRMED', 5.50, 'Washing', NOW(), NOW());