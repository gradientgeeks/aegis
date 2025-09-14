
# Aegis Security Environment

## Overview

Aegis Security Environment is an enterprise-grade, multi-layered security platform designed to protect mobile banking applications from fraud and unauthorized access. It implements advanced cryptographic protocols, device fingerprinting, and policy-based security enforcement to ensure end-to-end protection of financial transactions.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [System Components](#system-components)
- [Security Features](#security-features)
- [Technology Stack](#technology-stack)
- [Installation & Setup](#installation--setup)
- [API Documentation](#api-documentation)
- [Security Flow](#security-flow)
- [Development](#development)
- [Deployment](#deployment)
- [Security Considerations](#security-considerations)
- [Contributing](#contributing)
- [License](#license)
- [Support](#support)
- [Acknowledgments](#acknowledgments)

---

## Architecture Overview

```mermaid
flowchart TD
    subgraph MobileApp[Mobile Application]
        SFEClient[SFE Client SDK]
        DemoBankApp[Demo Banking App]
    end

    subgraph BankBackend[Demo Bank Backend]
        Auth[User Authentication]
        Accounts[Accounts & Transactions]
        Integration[Aegis API Integration]
    end

    subgraph AegisAPI[Aegis Security API]
        Controllers[Controllers]
        Services[Services]
        Repositories[Repositories]
        Entities[Entities]
    end

    subgraph DataLayer[Data Layer]
        PostgreSQL[(PostgreSQL/TiDB)]
        Redis[(Redis Cache)]
    end

    subgraph Admin[Admin & Monitoring]
        Portal[Admin Portal - React]
        Dashboard[Bank Dashboard - React]
    end

    SFEClient -->|Provision/Sign| AegisAPI
    DemoBankApp --> BankBackend --> AegisAPI
    AegisAPI --> DataLayer
    Portal --> AegisAPI
    Dashboard --> BankBackend
````

---

## System Components

### 1. Aegis Security API (`/aegis`)

The core backend providing cryptographic services and device management.

**Key Features:**

* Device registration and provisioning
* HMAC-SHA256 signature validation
* Policy-based security enforcement
* Real-time fraud detection
* Device fingerprinting and tracking
* Admin management interface

**Architecture Layers:**

* **Controller Layer**
  `DeviceController`, `AuthController`, `AdminController`, `PolicyController`, `FraudController`
* **Service Layer**
  `CryptographyService`, `DeviceRegistrationService`, `SignatureValidationService`,
  `PolicyEnforcementService`, `DeviceFraudDetectionService`, `IntegrityValidationService`
* **Repository Layer (Spring Data JPA)**
  `DeviceRepository`, `UserRepository`, `PolicyRepository`,
  `DeviceFingerprintRepository`, `PolicyViolationRepository`
* **Entity Layer**
  `Device`, `DeviceFingerprint`, `Policy`, `PolicyRule`, `PolicyViolation`

---

### 2. Demo Bank Backend (`/backend-app`)

Simulates a bank backend system integrated with Aegis.

**Key Features:**

* User authentication and session management
* Account and balance tracking
* Transaction processing with signature validation
* Device rebinding support
* KYC data management

---

### 3. Android Security Framework (`/sfe`)

#### 3.1 SFE Client SDK (`/sfe/sfe-client`)

Headless Android library providing cryptographic and security services.

**Key Features:**

* Secure device provisioning
* HMAC-SHA256 signing
* AES-256 encryption & RSA envelope encryption
* Android Keystore integration
* SecureVaultService for sensitive data
* Device fingerprinting

**Core Classes:**

```kotlin
AegisSfeClient - Main SDK interface
SecureKeyStorage - Android Keystore wrapper
RequestSigningService - HMAC signing implementation
SecureVaultService - Encrypted storage service
DeviceFingerprintCollector - Device characteristic gathering
UserMetadataCollector - User context collection
```

#### 3.2 Demo Banking App (`/sfe/app`)

Demo Android app showcasing SDK integration.

**Features:**

* Biometric authentication
* Account dashboard
* Secure transfers & transaction history
* Device provisioning UI
* Jetpack Compose UI

---

### 4. Admin Portal (`/aegis-portal`)

React-based administrative dashboard.

**Features:**

* Device management
* Policy configuration
* Fraud detection analytics
* Real-time alerts & audit logs

---

### 5. Bank Dashboard (`/bank-dashboard`)

React-based operations monitoring dashboard.

---

## Security Features

### Cryptographic Implementation

#### 1. Key Generation

```java
SecureRandom secureRandom = new SecureRandom();
String secretKey = new BigInteger(256, secureRandom).toString(32);
```

#### 2. HMAC-SHA256 Signing

```java
String stringToSign = method + "|" + path + "|" + timestamp + "|" + nonce + "|" + body;
Mac mac = Mac.getInstance("HmacSHA256");
SecretKeySpec spec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
mac.init(spec);
String signature = Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
```

#### 3. Secure Storage (Android)

* Android Keystore
* AES-256-GCM encryption
* RSA-2048 key wrapping
* Envelope encryption pattern

---

### Device Security

* **Fingerprinting:** Hardware, software, network, display parameters
* **Policy Enforcement:** Real-time rules & violations
* **Fraud Detection:** Fingerprint changes, geo anomalies, patterns, biometrics

---

## Technology Stack

* **Backend:** Spring Boot 3.5.3 (Java 21), PostgreSQL/TiDB, Redis, JWT, Gradle
* **Android:** Kotlin, Jetpack Compose, MVVM, Retrofit, Hilt
* **Web:** React 18, TypeScript, Redux Toolkit, Material-UI, Axios

---

## Security Flow

### 1. Device Registration

```
App → Backend → Aegis API → Device ID + Secret → Keystore
```

### 2. Secure Transaction

```
App Signs (HMAC) → Backend Validates via Aegis → Result
```

### 3. Policy Enforcement

```
Request → Fingerprint → Policy Rules → Risk Score → Allow/Deny
```

---

## Installation & Setup

### Prerequisites

* Java 21
* Android Studio
* Node.js 18+
* PostgreSQL / TiDB
* Redis

### Quick Start

```bash
git clone https://github.com/gradientgeeks/aegis.git
cd aegis
./gradlew bootRun
```

### Database Setup

```sql
CREATE DATABASE aegis_security_v3;
```

Run migrations (auto on first boot).
Optional demo data:

```bash
./gradlew bootRun --args="--spring.profiles.active=demo"
```

---

## API Documentation

### Device Registration

```http
POST /api/device/register
Content-Type: application/json

{
  "clientId": "UCOBANK_PROD_ANDROID",
  "registrationKey": "REG-KEY-123",
  "integrityToken": "play-integrity-token",
  "deviceFingerprint": { ... }
}
```

### Signature Validation

```http
POST /api/device/validate-signature
Headers:
  X-Device-Id: device-uuid
  X-Signature: base64-hmac-signature
  X-Timestamp: 1234567890
  X-Nonce: unique-nonce

{
  "data": "request-body"
}
```

Full docs: `/api/swagger-ui.html`

---

## Development

### Backend

```bash
./gradlew build
./gradlew test
./gradlew jacocoTestReport
```

### Android

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew test
./gradlew connectedAndroidTest
```

---

## Deployment

### Docker

```bash
docker build -t aegis-api ./aegis
docker build -t bank-backend ./backend-app
docker-compose up -d
```

### Azure

```bash
./deploy-azure-cloud-build.sh
./monitor-azure.sh
```

### Environment Variables

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/aegis
SPRING_DATASOURCE_USERNAME=aegis_user
SPRING_DATASOURCE_PASSWORD=secure_password
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
JWT_SECRET=your-secret-key
```

---

## Security Considerations

* Enforce HTTPS/TLS
* Rate limiting & audit logging
* Key rotation with HSM storage
* Fraud monitoring & anomaly detection
* OWASP MASVS compliance

---


## License

MIT License


---

**Version:** 1.0.7
**Last Updated:** September 2025
**Maintained by:** Gradient Geeks


