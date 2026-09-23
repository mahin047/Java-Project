-- =====================================================
-- SlotSync Database Schema
-- =====================================================
CREATE DATABASE IF NOT EXISTS slotsync_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE slotsync_db;

-- ---------- USERS ----------
CREATE TABLE IF NOT EXISTS users (
                                     id            INT AUTO_INCREMENT PRIMARY KEY,
                                     full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    student_id    VARCHAR(30)  UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          ENUM('STUDENT', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ) ENGINE = InnoDB;

-- ---------- RESOURCES ----------
CREATE TABLE IF NOT EXISTS resources (
                                         id          INT AUTO_INCREMENT PRIMARY KEY,
                                         name        VARCHAR(100) NOT NULL UNIQUE,
    type        VARCHAR(30)  NOT NULL,          -- LAB, ROOM, EQUIPMENT ...
    location    VARCHAR(100),
    capacity    INT NOT NULL DEFAULT 1,
    description VARCHAR(255),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ) ENGINE = InnoDB;

-- ---------- BOOKINGS ----------
-- ---------- BOOKINGS ----------
CREATE TABLE IF NOT EXISTS bookings (
                                        id           INT AUTO_INCREMENT PRIMARY KEY,
                                        user_id      INT NOT NULL,
                                        resource_id  INT NOT NULL,
                                        booking_date DATE NOT NULL,
                                        start_time   TIME NOT NULL,
                                        end_time     TIME NOT NULL,
                                        status       ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')
    NOT NULL DEFAULT 'CONFIRMED',
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- NULL for cancelled bookings, so a cancelled slot can be re-booked
    active_slot_key VARCHAR(80)
    GENERATED ALWAYS AS (
                            CASE WHEN status <> 'CANCELLED'
                            THEN CONCAT(resource_id, '|', booking_date, '|', start_time)
    ELSE NULL END
    ) STORED,

    CONSTRAINT fk_booking_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_resource
    FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    CONSTRAINT chk_time CHECK (end_time > start_time),
    CONSTRAINT uq_active_slot UNIQUE (active_slot_key),

    INDEX idx_booking_user (user_id),
    INDEX idx_booking_date (booking_date)
    ) ENGINE = InnoDB;

-- ---------- NOTIFICATIONS ----------
CREATE TABLE IF NOT EXISTS notifications (
                                             id         INT AUTO_INCREMENT PRIMARY KEY,
                                             user_id    INT NOT NULL,
                                             message    VARCHAR(255) NOT NULL,
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notif_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_user (user_id)
    ) ENGINE = InnoDB;

-- ---------- SAMPLE RESOURCES ----------
INSERT IGNORE INTO resources (name, type, location, capacity, description) VALUES
    ('Computer Lab 1',    'LAB',       'Building A, Room 101', 40, 'General purpose computer lab'),
    ('Computer Lab 2',    'LAB',       'Building A, Room 102', 30, 'Programming lab'),
    ('Seminar Room',      'ROOM',      'Building B, Room 201', 60, 'Seminar and presentation room'),
    ('Discussion Room 1', 'ROOM',      'Library, 2nd Floor',    8, 'Group study room'),
    ('Projector Set',     'EQUIPMENT', 'Store Room',            1, 'Portable projector with screen');