# Demo Users KYC Information

This document contains the KYC (Know Your Customer) information for demo users in the UCO Bank Backend application. This information is required for device rebinding verification.

## Demo User 1: `demo1`

- **Username**: demo1
- **Password**: password123
- **Full Name**: Anurag Sharma
- **Email**: demo1@ucobank.com

### KYC Information:
- **Aadhaar Last 4 Digits**: 1234
- **PAN Number**: ABCDE1234F

### Security Questions:
- **Mother's Maiden Name**: sharma
- **First School Name**: dps

---

## Demo User 2: `demo2`

- **Username**: demo2
- **Password**: password123
- **Full Name**: Priya Patel
- **Email**: demo2@ucobank.com

### KYC Information:
- **Aadhaar Last 4 Digits**: 5678
- **PAN Number**: FGHIJ5678K

### Security Questions:
- **Mother's Maiden Name**: patel
- **First School Name**: kvs

---

## Device Rebinding Process

When a user attempts to login from a new device after being bound to another device, they must complete identity verification:

1. The system detects the device change and prompts for verification
2. User must provide:
   - Last 4 digits of Aadhaar
   - Complete PAN number
   - Answers to security questions
3. Upon successful verification, the old device binding is replaced with the new device

## Important Notes

- All security answers are case-insensitive and trimmed of whitespace
- PAN numbers must be in valid format: 5 letters + 4 numbers + 1 letter
- Device rebinding is logged for audit purposes
- Multiple failed attempts may result in account lockout (configurable)