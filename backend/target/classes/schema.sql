-- This table is correct
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- This table is correct
CREATE TABLE IF NOT EXISTS machines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    machine_number VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    location VARCHAR(255),
    description TEXT,
    machine_type VARCHAR(255),
    brand VARCHAR(255),
    model VARCHAR(255),
    capacity VARCHAR(100),
    features TEXT,
    price_per_hour DOUBLE NOT NULL DEFAULT 0.0,
    price_per_day DOUBLE NOT NULL DEFAULT 0.0,
    current_user_id BIGINT,
    usage_start_time DATETIME,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_current_user
        FOREIGN KEY (current_user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


-- This table is correct
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    machine_id BIGINT NOT NULL,
    booking_date TIMESTAMP NOT NULL,
    
    confirmation_expiry_time TIMESTAMP,
    rating INT,
    
    amount DOUBLE NOT NULL,
    service VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (machine_id) REFERENCES machines(id)
);

-- This table is correct
CREATE TABLE IF NOT EXISTS booking_status (
    booking_id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_booking_status_to_booking
        FOREIGN KEY (booking_id)
        REFERENCES bookings(id)
        ON DELETE CASCADE
);


-- This table is correct
CREATE TABLE IF NOT EXISTS ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    machine_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL UNIQUE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_text TEXT,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (machine_id) REFERENCES machines(id),
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

