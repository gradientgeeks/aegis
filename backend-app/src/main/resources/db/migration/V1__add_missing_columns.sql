-- Add missing columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;

-- Create user_devices table for device associations
CREATE TABLE IF NOT EXISTS user_devices (
    user_id BIGINT NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, device_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);