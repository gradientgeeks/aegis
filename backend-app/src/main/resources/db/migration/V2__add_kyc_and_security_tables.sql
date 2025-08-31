-- Create user_kyc table for storing KYC information
CREATE TABLE IF NOT EXISTS user_kyc (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    aadhaar_last4 VARCHAR(4) NOT NULL,
    pan_number VARCHAR(10) NOT NULL,
    aadhaar_hash VARCHAR(255),
    is_verified BOOLEAN DEFAULT FALSE,
    verification_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_pan_number (pan_number)
);

-- Create security_questions table for storing security Q&A
CREATE TABLE IF NOT EXISTS security_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_key VARCHAR(50) NOT NULL,
    question_text VARCHAR(255) NOT NULL,
    answer_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_question (user_id, question_key),
    UNIQUE KEY uk_user_question_key (user_id, question_key)
);

-- Add device rebinding audit table
CREATE TABLE IF NOT EXISTS device_rebinding_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    old_device_id VARCHAR(255),
    new_device_id VARCHAR(255) NOT NULL,
    verification_method VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    success BOOLEAN NOT NULL DEFAULT FALSE,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_rebinding (user_id),
    INDEX idx_device_rebinding (new_device_id),
    INDEX idx_rebinding_date (created_at)
);