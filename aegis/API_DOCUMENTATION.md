# Aegis Security API Documentation

## Overview

The Aegis Security API provides comprehensive device security and fraud detection services for mobile applications. It includes device registration, cryptographic signature validation, organization management, and advanced fraud detection capabilities.

**Base URL:** `http://localhost:8080/api`

**API Version:** v1

## Table of Contents

1. [Authentication](#authentication)
2. [Device Management](#device-management)
3. [Admin Operations](#admin-operations)
4. [Fraud Detection](#fraud-detection)
5. [Common Data Types](#common-data-types)
6. [Error Handling](#error-handling)
7. [Security Headers](#security-headers)

---

## Authentication

The Aegis API uses JWT tokens for authentication. Obtain a token by logging in with valid credentials.

### Login

Authenticate a user and receive a JWT token.

**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "email": "admin@aegissecurity.com",
  "password": "SecurePass123!"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "admin@aegissecurity.com",
    "name": "Admin User",
    "organization": "Aegis Security",
    "role": "ADMIN",
    "status": "ACTIVE"
  },
  "expiresAt": "2025-01-30T10:00:00Z"
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid credentials
- `403 Forbidden` - Account not active or approved

### Register Organization

Register a new organization that will use Aegis security services.

**Endpoint:** `POST /auth/register`

**Request Body:**
```json
{
  "organizationName": "UCO Bank",
  "email": "security@ucobank.com",
  "password": "SecurePass123!",
  "contactPerson": "John Doe",
  "phone": "+1-555-0123",
  "address": "123 Bank Street, Financial District, NY 10004"
}
```

**Response:** `201 Created`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 2,
    "email": "security@ucobank.com",
    "name": "John Doe",
    "organization": "UCO Bank",
    "role": "USER",
    "status": "PENDING_APPROVAL"
  },
  "expiresAt": "2025-01-30T10:00:00Z"
}
```

**Note:** New organizations require admin approval before they can access full functionality.

### Health Check

Check authentication service status.

**Endpoint:** `GET /auth/health`

**Response:** `200 OK`
```json
{
  "status": "ok",
  "message": "Authentication service is running"
}
```

---

## Device Management

Core endpoints for device registration and signature validation.

### Register Device

Register a new device with Aegis security system.

**Endpoint:** `POST /v1/register`

**Request Body:**
```json
{
  "clientId": "ucobank",
  "registrationKey": "REG-2025-01-29-abc123def456...",
  "integrityToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "deviceFingerprint": {
    "hardwareFingerprint": {
      "manufacturer": "Samsung",
      "brand": "Samsung",
      "model": "SM-G991B",
      "device": "Galaxy S21",
      "board": "exynos2100",
      "hardware": "exynos2100",
      "androidId": "a1b2c3d4e5f67890",
      "serialNumber": "R58M1234567",
      "buildId": "SP1A.210812.016",
      "bootloader": "G991BXXU4CWA1",
      "cpuInfo": "AArch64 Processor rev 1 (aarch64)",
      "supportedAbis": ["arm64-v8a", "armeabi-v7a", "armeabi"]
    },
    "displayFingerprint": {
      "width": 1080,
      "height": 2400,
      "density": 420,
      "scaledDensity": 420,
      "xdpi": 422.03,
      "ydpi": 424.069,
      "refreshRate": 120.0
    },
    "networkFingerprint": {
      "networkType": "WIFI",
      "networkOperator": "Verizon",
      "networkOperatorName": "Verizon Wireless",
      "simOperator": "311480",
      "simOperatorName": "Verizon",
      "phoneType": "GSM",
      "dataState": "CONNECTED",
      "simState": "READY",
      "isRoaming": false
    },
    "sensorFingerprint": {
      "accelerometer": true,
      "gyroscope": true,
      "magnetometer": true,
      "proximity": true,
      "light": true,
      "pressure": true,
      "temperature": false,
      "humidity": false,
      "fingerprint": true,
      "face": true,
      "iris": false
    },
    "appVersion": "1.0.0",
    "sdkVersion": "1.0.0"
  }
}
```

**Response:** `200 OK`
```json
{
  "status": "success",
  "message": "Device registered successfully",
  "deviceId": "DEV-2025-01-29-123456",
  "secretKey": "BASE64_ENCODED_SECRET_KEY",
  "hmacKey": "BASE64_ENCODED_HMAC_KEY",
  "createdAt": "2025-01-29T10:30:00Z"
}
```

**Error Responses:**
- `400 Bad Request` - Invalid registration key or client ID
- `401 Unauthorized` - Registration key expired or revoked
- `409 Conflict` - Device already registered

### Validate Signature

Validate HMAC signature for a transaction or API request.

**Endpoint:** `POST /v1/validate`

**Request Body:**
```json
{
  "deviceId": "DEV-2025-01-29-123456",
  "signature": "hmacSha256SignatureBase64Encoded",
  "stringToSign": "POST|/api/transfer|timestamp=1706526600000|nonce=abc123|body={\"amount\":100,\"to\":\"12345\"}",
  "clientId": "ucobank",
  "ipAddress": "192.168.1.100",
  "userAgent": "AegisSDK/1.0.0 (Android 13; Samsung SM-G991B)"
}
```

**Response:** `200 OK`
```json
{
  "valid": true,
  "message": "Signature validation successful",
  "deviceStatus": "ACTIVE",
  "riskScore": 15,
  "validatedAt": "2025-01-29T10:35:00Z"
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid signature
- `403 Forbidden` - Device blocked or fraudulent
- `404 Not Found` - Device not registered

### Health Check

Check device management service status.

**Endpoint:** `GET /v1/health`

**Response:** `200 OK`
```text
Aegis Security API is running
```

---

## Admin Operations

Administrative endpoints for managing organizations, registration keys, and devices.

### Registration Key Management

#### Generate Registration Key

Create a new registration key for device onboarding.

**Endpoint:** `POST /admin/registration-keys`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Request Body:**
```json
{
  "clientId": "ucobank",
  "description": "Mobile app production deployment Q1 2025",
  "expiresAt": "2025-12-31T23:59:59Z"
}
```

**Response:** `201 Created`
```json
{
  "status": "success",
  "message": "Registration key generated successfully",
  "clientId": "ucobank",
  "registrationKey": "REG-2025-01-29-abc123def456...",
  "organization": "UCO Bank",
  "description": "Mobile app production deployment Q1 2025",
  "createdAt": "2025-01-29T10:00:00Z",
  "expiresAt": "2025-12-31T23:59:59Z",
  "status": "ACTIVE",
  "createdBy": "security@ucobank.com"
}
```

#### List Registration Keys

Get all registration keys for your organization.

**Endpoint:** `GET /admin/registration-keys`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Response:** `200 OK`
```json
[
  {
    "clientId": "ucobank",
    "registrationKey": "REG-2025-01-29-abc123def456...",
    "organization": "UCO Bank",
    "description": "Mobile app production deployment Q1 2025",
    "createdAt": "2025-01-29T10:00:00Z",
    "expiresAt": "2025-12-31T23:59:59Z",
    "status": "ACTIVE",
    "usageCount": 150,
    "lastUsedAt": "2025-01-29T09:45:00Z"
  }
]
```

**Note:** Admin users can see all keys but registration keys are hidden from admin view for security.

#### Get Registration Key Details

Retrieve details of a specific registration key.

**Endpoint:** `GET /admin/registration-keys/{clientId}`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Response:** `200 OK`
```json
{
  "clientId": "ucobank",
  "registrationKey": "REG-2025-01-29-abc123def456...",
  "organization": "UCO Bank",
  "description": "Mobile app production deployment Q1 2025",
  "createdAt": "2025-01-29T10:00:00Z",
  "expiresAt": "2025-12-31T23:59:59Z",
  "status": "ACTIVE",
  "usageCount": 150,
  "devices": [
    {
      "deviceId": "DEV-2025-01-29-123456",
      "registeredAt": "2025-01-29T09:45:00Z",
      "status": "ACTIVE"
    }
  ]
}
```

#### Revoke Registration Key

Revoke a registration key to prevent new device registrations.

**Endpoint:** `PUT /admin/registration-keys/{clientId}/revoke`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Response:** `200 OK`
```json
{
  "status": "success",
  "message": "Registration key revoked successfully",
  "clientId": "ucobank",
  "revokedAt": "2025-01-29T11:00:00Z",
  "status": "REVOKED"
}
```

#### Regenerate Registration Key

Generate a new registration key for a client ID.

**Endpoint:** `PUT /admin/registration-keys/{clientId}/regenerate`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Response:** `200 OK`
```json
{
  "status": "success",
  "message": "Registration key regenerated successfully",
  "clientId": "ucobank",
  "registrationKey": "REG-2025-01-29-xyz789uvw012...",
  "createdAt": "2025-01-29T11:05:00Z",
  "expiresAt": "2025-12-31T23:59:59Z",
  "status": "ACTIVE"
}
```

### Organization Management

#### Get Pending Organizations (Admin Only)

List all organizations awaiting approval.

**Endpoint:** `GET /admin/organizations/pending`

**Headers:**
- `Authorization: Bearer {ADMIN_JWT_TOKEN}`

**Response:** `200 OK`
```json
[
  {
    "id": 3,
    "organization": "First National Bank",
    "email": "security@fnb.com",
    "contactPerson": "Jane Smith",
    "phone": "+1-555-0456",
    "address": "456 Finance Ave, NY 10005",
    "status": "PENDING_APPROVAL",
    "registeredAt": "2025-01-29T08:00:00Z"
  }
]
```

#### Get All Organizations (Admin Only)

List all registered organizations.

**Endpoint:** `GET /admin/organizations`

**Headers:**
- `Authorization: Bearer {ADMIN_JWT_TOKEN}`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "organization": "Aegis Security",
    "email": "admin@aegissecurity.com",
    "contactPerson": "Admin User",
    "status": "ACTIVE",
    "role": "ADMIN",
    "registeredAt": "2025-01-01T00:00:00Z",
    "approvedAt": "2025-01-01T00:00:00Z",
    "approvedBy": "SYSTEM"
  },
  {
    "id": 2,
    "organization": "UCO Bank",
    "email": "security@ucobank.com",
    "contactPerson": "John Doe",
    "status": "ACTIVE",
    "role": "USER",
    "registeredAt": "2025-01-15T10:00:00Z",
    "approvedAt": "2025-01-15T10:30:00Z",
    "approvedBy": "admin@aegissecurity.com"
  }
]
```

#### Approve Organization (Admin Only)

Approve a pending organization registration.

**Endpoint:** `POST /admin/organizations/{userId}/approve`

**Headers:**
- `Authorization: Bearer {ADMIN_JWT_TOKEN}`

**Request Body:**
```json
{
  "approvedBy": "admin@aegissecurity.com"
}
```

**Response:** `200 OK`
```json
{
  "id": 3,
  "organization": "First National Bank",
  "email": "security@fnb.com",
  "contactPerson": "Jane Smith",
  "status": "ACTIVE",
  "approvedAt": "2025-01-29T12:00:00Z",
  "approvedBy": "admin@aegissecurity.com"
}
```

#### Reject Organization (Admin Only)

Reject a pending organization registration.

**Endpoint:** `POST /admin/organizations/{userId}/reject`

**Headers:**
- `Authorization: Bearer {ADMIN_JWT_TOKEN}`

**Request Body:**
```json
{
  "approvedBy": "admin@aegissecurity.com"
}
```

**Response:** `200 OK`
```json
{
  "id": 3,
  "organization": "Suspicious Corp",
  "email": "admin@suspicious.com",
  "status": "REJECTED",
  "rejectedAt": "2025-01-29T12:05:00Z",
  "rejectedBy": "admin@aegissecurity.com"
}
```

### Device Management

#### Block Device

Block a device from making transactions.

**Endpoint:** `POST /admin/devices/{deviceId}/block`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Request Body:**
```json
{
  "reason": "Suspicious activity detected - multiple failed authentication attempts",
  "blockType": "TEMPORARILY_BLOCKED"
}
```

**Response:** `200 OK`
```json
{
  "status": "success",
  "message": "Device blocked successfully",
  "deviceId": "DEV-2025-01-29-123456",
  "blockType": "TEMPORARILY_BLOCKED"
}
```

**Block Types:**
- `TEMPORARILY_BLOCKED` - Temporary block, can be unblocked
- `PERMANENTLY_BLOCKED` - Permanent block, requires admin intervention
- `FRAUDULENT` - Marked as fraudulent device

#### Unblock Device

Unblock a previously blocked device. Admins can unblock any device, while banks can only unblock devices that have interacted with their applications.

**Endpoint:** `POST /admin/devices/{deviceId}/unblock`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Request Body:**
```json
{
  "reason": "False positive - legitimate user verified"
}
```

**Response:** `200 OK`
```json
{
  "status": "success",
  "message": "Device unblocked successfully",
  "deviceId": "DEV-2025-01-29-123456",
  "unblockedBy": "UCO Bank"
}
```

**Error Responses:**
- `403 Forbidden` - Bank attempted to unblock device with no interaction history
- `404 Not Found` - Device not found

**Authorization:**
- Admin users can unblock any device
- Bank users can only unblock devices that have used their registered client IDs

#### Report Fraud

Report a device for fraudulent activity.

**Endpoint:** `POST /admin/fraud-report`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Request Body:**
```json
{
  "deviceId": "DEV-2025-01-29-123456",
  "bankTransactionId": "TXN-2025-01-29-789012",
  "reasonCode": "BANK_ML_HIGH_RISK",
  "description": "Transaction pattern matches known fraud behavior - rapid small transactions followed by large withdrawal"
}
```

**Response:** `200 OK`
```json
{
  "status": "success",
  "message": "Fraud report processed",
  "deviceId": "DEV-2025-01-29-123456",
  "action": "Device status updated"
}
```

**Reason Codes:**
- `BANK_ML_HIGH_RISK` - Bank's ML model detected high risk
- `CONFIRMED_FRAUD` - Confirmed fraudulent transaction
- `SUSPICIOUS_PATTERN` - Suspicious transaction patterns
- `ACCOUNT_TAKEOVER` - Suspected account takeover
- `DEVICE_COMPROMISE` - Device suspected to be compromised

---

## Fraud Detection

Advanced fraud detection and monitoring endpoints.

### Get Fraud Statistics

Retrieve fraud detection statistics.

**Endpoint:** `GET /admin/fraud/statistics`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Query Parameters:**
- `period` (optional): Time period - "24h", "7d", "30d" (default: "30d")

**Response:** `200 OK`
```json
{
  "totalDevices": 15420,
  "blockedDevices": 342,
  "fraudulentDevices": 89,
  "recentReports": 156,
  "period": "30d",
  "generatedAt": 1706526600000,
  "organizationBreakdown": {
    "UCO Bank": {
      "total": 8500,
      "blocked": 120,
      "fraudulent": 25
    },
    "First National Bank": {
      "total": 6920,
      "blocked": 222,
      "fraudulent": 64
    }
  }
}
```

**Note:** Regular users only see their organization's statistics.

### Get Recent Fraud Reports

Retrieve recent fraud reports.

**Endpoint:** `GET /admin/fraud/reports/recent`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Query Parameters:**
- `limit` (optional): Number of reports to retrieve (default: 10)

**Response:** `200 OK`
```json
[
  {
    "reportId": "RPT-2025-01-29-001",
    "deviceId": "DEV-2025-01-29-123456",
    "organization": "UCO Bank",
    "reasonCode": "BANK_ML_HIGH_RISK",
    "description": "Suspicious transaction pattern detected",
    "reportedAt": "2025-01-29T10:00:00Z",
    "reportedBy": "security@ucobank.com",
    "status": "PROCESSED",
    "action": "DEVICE_BLOCKED"
  }
]
```

### Search Devices

Search and filter devices with pagination.

**Endpoint:** `GET /admin/devices/search`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Query Parameters:**
- `deviceId` (optional): Device ID to search for
- `clientId` (optional): Client/Organization ID to filter by
- `status` (optional): Device status filter (ACTIVE, BLOCKED, FRAUDULENT)
- `page` (optional): Page number (0-based, default: 0)
- `size` (optional): Page size (default: 20)

**Response:** `200 OK`
```json
{
  "devices": [
    {
      "deviceId": "DEV-2025-01-29-123456",
      "clientId": "ucobank",
      "status": "ACTIVE",
      "registrationDate": "2025-01-29T09:45:00Z",
      "lastActivity": "2025-01-29T11:30:00Z",
      "isFraudulent": false,
      "hardwareInfo": {
        "manufacturer": "Samsung",
        "model": "SM-G991B",
        "device": "Galaxy S21"
      }
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 1542,
    "totalPages": 78,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### Get Device Fraud Status

Check detailed fraud status of a specific device.

**Endpoint:** `GET /admin/devices/{deviceId}/fraud-status`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Response:** `200 OK`
```json
{
  "deviceId": "DEV-2025-01-29-123456",
  "status": "ACTIVE",
  "organization": "ucobank",
  "registrationDate": "2025-01-29T09:45:00Z",
  "lastValidation": "2025-01-29T11:30:00Z",
  "isFraudulent": false,
  "fraudReportedAt": null,
  "fraudReason": null,
  "hardwareInfo": {
    "manufacturer": "Samsung",
    "model": "SM-G991B",
    "device": "Galaxy S21"
  }
}
```

### Mark Device as Fraudulent

Manually mark a device as fraudulent.

**Endpoint:** `POST /admin/devices/{deviceId}/mark-fraudulent`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Request Body:**
```json
{
  "reason": "Confirmed fraudulent activity - user reported unauthorized transactions"
}
```

**Response:** `200 OK`
```json
{
  "status": "success",
  "message": "Device marked as fraudulent",
  "deviceId": "DEV-2025-01-29-123456"
}
```

### Get Device History

Retrieve transaction validation history for a device.

**Endpoint:** `GET /admin/devices/{deviceId}/history`

**Headers:**
- `Authorization: Bearer {JWT_TOKEN}`

**Query Parameters:**
- `page` (optional): Page number (0-based, default: 0)
- `size` (optional): Page size (default: 20)

**Response:** `200 OK`
```json
{
  "deviceId": "DEV-2025-01-29-123456",
  "validations": [
    {
      "validationId": "VAL-2025-01-29-001",
      "timestamp": "2025-01-29T11:30:00Z",
      "result": "SUCCESS",
      "riskScore": 15,
      "clientId": "ucobank",
      "ipAddress": "192.168.1.100"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

---

## Common Data Types

### Device Fingerprint Structure

```json
{
  "hardwareFingerprint": {
    "manufacturer": "string",
    "brand": "string", 
    "model": "string",
    "device": "string",
    "board": "string",
    "hardware": "string",
    "androidId": "string",
    "serialNumber": "string",
    "buildId": "string",
    "bootloader": "string",
    "cpuInfo": "string",
    "supportedAbis": ["string"]
  },
  "displayFingerprint": {
    "width": "integer",
    "height": "integer",
    "density": "integer",
    "scaledDensity": "integer",
    "xdpi": "float",
    "ydpi": "float",
    "refreshRate": "float"
  },
  "networkFingerprint": {
    "networkType": "string",
    "networkOperator": "string",
    "networkOperatorName": "string",
    "simOperator": "string",
    "simOperatorName": "string",
    "phoneType": "string",
    "dataState": "string",
    "simState": "string",
    "isRoaming": "boolean"
  },
  "sensorFingerprint": {
    "accelerometer": "boolean",
    "gyroscope": "boolean",
    "magnetometer": "boolean",
    "proximity": "boolean",
    "light": "boolean",
    "pressure": "boolean",
    "temperature": "boolean",
    "humidity": "boolean",
    "fingerprint": "boolean",
    "face": "boolean",
    "iris": "boolean"
  },
  "appVersion": "string",
  "sdkVersion": "string"
}
```

### Device Status Values

- `ACTIVE` - Device is active and can perform transactions
- `TEMPORARILY_BLOCKED` - Device is temporarily blocked
- `PERMANENTLY_BLOCKED` - Device is permanently blocked
- `FRAUDULENT` - Device is marked as fraudulent
- `PENDING_VERIFICATION` - Device pending verification

### User Roles

- `ADMIN` - System administrator with full access
- `USER` - Organization user with limited access

### Organization Status Values

- `PENDING_APPROVAL` - Awaiting admin approval
- `ACTIVE` - Approved and active
- `SUSPENDED` - Temporarily suspended
- `REJECTED` - Registration rejected

---

## Error Handling

All API errors follow a consistent format:

```json
{
  "status": "error",
  "message": "Human-readable error description",
  "code": "ERROR_CODE",
  "timestamp": "2025-01-29T10:00:00Z"
}
```

### Common Error Codes

- `INVALID_CREDENTIALS` - Authentication failed
- `UNAUTHORIZED` - Missing or invalid token
- `FORBIDDEN` - Insufficient permissions
- `NOT_FOUND` - Resource not found
- `CONFLICT` - Resource already exists
- `VALIDATION_ERROR` - Request validation failed
- `INTERNAL_ERROR` - Server error

### HTTP Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid request
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Access denied
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict
- `500 Internal Server Error` - Server error

---

## Security Headers

### Required Headers for Authenticated Requests

- `Authorization: Bearer {JWT_TOKEN}` - JWT authentication token

### Optional Security Headers

- `X-Device-Id` - Device identifier for additional validation
- `X-Signature` - Request signature for enhanced security
- `X-Timestamp` - Request timestamp to prevent replay attacks
- `X-Nonce` - Unique request identifier

### CORS Configuration

The API allows cross-origin requests from:
- `http://localhost:5173` - Admin portal
- `http://localhost:3000` - Development servers
- `http://localhost:8081` - Bank backend services

Allowed methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`

---

## Rate Limiting

API rate limits (when enabled):
- Authentication endpoints: 10 requests per minute
- Device registration: 100 requests per hour per organization
- Signature validation: 1000 requests per minute per device
- Admin endpoints: 100 requests per minute

---

## Integration Guide

### Step 1: Organization Registration

1. Register your organization via `/auth/register`
2. Wait for admin approval
3. Once approved, login to get JWT token

### Step 2: Generate Registration Keys

1. Use `/admin/registration-keys` to create registration keys
2. Distribute keys securely to your mobile app

### Step 3: Device Registration

1. Mobile app uses registration key to register device
2. Store returned `deviceId` and `secretKey` securely
3. Use `hmacKey` for request signing

### Step 4: Request Signing

1. Create string to sign: `METHOD|PATH|timestamp=TIMESTAMP|nonce=NONCE|body=BODY`
2. Generate HMAC-SHA256 signature using `hmacKey`
3. Include signature in validation requests

### Step 5: Signature Validation

1. Backend calls `/v1/validate` with signature
2. Aegis validates and returns device status
3. Backend proceeds based on validation result

---

## Support

For technical support and questions:
- Email: support@aegissecurity.com
- Documentation: https://docs.aegissecurity.com
- API Status: https://status.aegissecurity.com