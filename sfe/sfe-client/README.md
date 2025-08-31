# Aegis SFE Client SDK

> ⚠️ **Hackathon Demo Project**: This SDK was created for demonstration purposes during a hackathon. It is not intended for production use.

## Overview

The Aegis SFE (Security Framework Extension) Client SDK is an Android library that provides comprehensive security features for mobile applications. It's part of the Aegis Security Environment ecosystem.

## Features

- Device provisioning and registration
- HMAC-SHA256 request signing
- Secure key storage using Android Keystore
- Envelope encryption (AES-256 + RSA) for sensitive data
- Session key management with ECDH key exchange
- Payload encryption with AES-256-GCM
- Environment security validation

## Installation

### Using JitPack

Add JitPack repository to your root build.gradle:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency to your app's build.gradle:

```gradle
dependencies {
    implementation 'com.github.gradientgeeks:aegis:1.0.0'
}
```

> Note: Replace `1.0.0` with the latest release tag

## Usage

### Initialize the SDK

```kotlin
import com.gradientgeeks.aegis.sfe_client.AegisSfeClient

// Initialize the client
val aegisClient = AegisSfeClient(
    context = applicationContext,
    aegisApiUrl = "https://your-aegis-api.com",
    apiToken = "your-api-token"
)
```

### Device Provisioning

```kotlin
// Provision device on first launch
aegisClient.provisionDevice { result ->
    result.onSuccess { deviceId ->
        Log.d("Aegis", "Device provisioned: $deviceId")
    }.onFailure { exception ->
        Log.e("Aegis", "Provisioning failed", exception)
    }
}
```

### Sign API Requests

```kotlin
// Sign a request before sending to your backend
val signedRequest = aegisClient.signRequest(
    method = "POST",
    endpoint = "/api/transfer",
    payload = jsonPayload
)
```

### Secure Storage

```kotlin
// Store sensitive data securely
aegisClient.secureVault.store("user_token", sensitiveData)

// Retrieve encrypted data
val data = aegisClient.secureVault.retrieve("user_token")
```

## Requirements

- Android API 28+ (Android 9.0+)
- Java 11

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Disclaimer

This is a hackathon demo project and should not be used in production environments. The security implementations are for demonstration purposes only.