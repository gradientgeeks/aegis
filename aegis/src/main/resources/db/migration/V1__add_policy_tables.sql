-- Add missing policy-related tables for Aegis security

-- Policy table for defining security policies
CREATE TABLE IF NOT EXISTS policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_name VARCHAR(100) NOT NULL,
    policy_type VARCHAR(50) NOT NULL,
    organization VARCHAR(100) NOT NULL,
    severity_level VARCHAR(50) NOT NULL,
    is_enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_policy_name_org (policy_name, organization)
);

-- Policy rules table for defining policy conditions
CREATE TABLE IF NOT EXISTS policy_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_id BIGINT NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    parameter VARCHAR(100) NOT NULL,
    operator VARCHAR(50) NOT NULL,
    value VARCHAR(255) NOT NULL,
    risk_score INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (policy_id) REFERENCES policy(id) ON DELETE CASCADE
);

-- Create indexes for policy tables
CREATE INDEX IF NOT EXISTS idx_policy_org ON policy(organization);
CREATE INDEX IF NOT EXISTS idx_policy_enabled ON policy(is_enabled);
CREATE INDEX IF NOT EXISTS idx_policy_rule_policy_id ON policy_rule(policy_id);

-- Policy violations table for tracking security incidents
CREATE TABLE IF NOT EXISTS policy_violations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL,
    anonymized_user_id VARCHAR(255),
    organization VARCHAR(100) NOT NULL,
    policy_id BIGINT NOT NULL,
    policy_rule_id BIGINT,
    action_taken VARCHAR(50) NOT NULL,
    request_details TEXT,
    violation_details TEXT,
    ip_address VARCHAR(100),
    user_agent VARCHAR(500),
    client_id VARCHAR(100),
    severity_score INT,
    risk_score INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (policy_id) REFERENCES policy(id),
    FOREIGN KEY (policy_rule_id) REFERENCES policy_rule(id)
);

-- Create indexes for policy violations
CREATE INDEX IF NOT EXISTS idx_violation_device_id ON policy_violations(device_id);
CREATE INDEX IF NOT EXISTS idx_violation_anonymized_user_id ON policy_violations(anonymized_user_id);
CREATE INDEX IF NOT EXISTS idx_violation_organization ON policy_violations(organization);
CREATE INDEX IF NOT EXISTS idx_violation_policy_id ON policy_violations(policy_id);
CREATE INDEX IF NOT EXISTS idx_violation_created_at ON policy_violations(created_at);
CREATE INDEX IF NOT EXISTS idx_violation_action_taken ON policy_violations(action_taken);

-- User device context table for tracking anonymized user patterns
CREATE TABLE IF NOT EXISTS user_device_context (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    anonymized_user_id VARCHAR(255) NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    organization VARCHAR(100) NOT NULL,
    client_id VARCHAR(100),
    
    -- User session context
    account_tier VARCHAR(50),
    account_age_months INT,
    kyc_level VARCHAR(50),
    
    -- Transaction velocity tracking
    daily_transaction_count INT DEFAULT 0,
    weekly_transaction_count INT DEFAULT 0,
    monthly_transaction_count INT DEFAULT 0,
    daily_amount_range VARCHAR(50),
    weekly_amount_range VARCHAR(50),
    last_transaction_at TIMESTAMP NULL,
    
    -- Transaction amount tracking
    daily_transaction_amount DOUBLE DEFAULT 0.0,
    weekly_transaction_amount DOUBLE DEFAULT 0.0,
    monthly_transaction_amount DOUBLE DEFAULT 0.0,
    
    -- Risk indicators
    is_location_changed BOOLEAN DEFAULT false,
    is_device_changed BOOLEAN DEFAULT false,
    is_dormant_account BOOLEAN DEFAULT false,
    risk_score INT DEFAULT 0,
    
    -- Activity tracking
    last_activity_at TIMESTAMP NULL,
    total_sessions BIGINT DEFAULT 0,
    failed_attempts_count INT DEFAULT 0,
    last_failed_attempt_at TIMESTAMP NULL,
    
    -- Pattern detection
    usual_transaction_time VARCHAR(255),
    unusual_patterns TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_user_device_org (anonymized_user_id, device_id, organization)
);

-- Create indexes for user device context
CREATE INDEX IF NOT EXISTS idx_udc_anonymized_user_id ON user_device_context(anonymized_user_id);
CREATE INDEX IF NOT EXISTS idx_udc_device_id ON user_device_context(device_id);
CREATE INDEX IF NOT EXISTS idx_udc_organization ON user_device_context(organization);
CREATE INDEX IF NOT EXISTS idx_udc_user_device_org ON user_device_context(anonymized_user_id, device_id, organization);
CREATE INDEX IF NOT EXISTS idx_udc_last_activity ON user_device_context(last_activity_at);