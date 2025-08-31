# Aegis Security Platform - Complete Setup Guide

## Overview

The Aegis Security Platform consists of:
1. **Aegis Backend API** - Core security API for device registration and signature validation
2. **Aegis Portal** - Web portal for banks/fintech to manage registration keys
3. **Demo Bank Backend** - Example implementation of a bank backend
4. **Android SDK & Demo App** - Mobile security SDK and demo banking app

## Backend Setup (Aegis API)

### What's New
- ✅ CORS configuration for cross-origin requests
- ✅ User authentication system
- ✅ Demo users initialized on startup
- ✅ Login endpoint at `/auth/login`

### Prerequisites
- Java 21
- PostgreSQL
- Redis

### Steps to Run

1. **Start PostgreSQL and Redis**:
```bash
# Using Docker
docker run -d --name aegis-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=aegis -p 5432:5432 postgres:15
docker run -d --name aegis-redis -p 6379:6379 redis:7
```

2. **Navigate to Aegis directory**:
```bash
cd /home/uttam/IdeaProjects/aegis
```

3. **Run the application**:
```bash
./gradlew bootRun
```

The API will start on `http://localhost:8080`

### Demo Users Created Automatically

| Email | Password | Role | Organization |
|-------|----------|------|--------------|
| admin@aegis.com | admin123 | ADMIN | Aegis Security |
| bank@ucobank.com | bank123 | USER | UCO Bank |
| fintech@paytm.com | fintech123 | USER | Paytm |

### Key Endpoints

- **Authentication**:
  - `POST /auth/login` - Login with email/password

- **Admin Endpoints**:
  - `POST /admin/registration-keys` - Create registration key
  - `GET /admin/registration-keys` - List all keys
  - `GET /admin/registration-keys/{clientId}` - Get specific key
  - `PUT /admin/registration-keys/{clientId}/revoke` - Revoke key
  - `PUT /admin/registration-keys/{clientId}/regenerate` - Regenerate key

- **Device Endpoints**:
  - `POST /v1/register` - Register device
  - `POST /v1/validate` - Validate signature

## Portal Setup (Aegis Portal)

### Steps to Run

1. **Navigate to Portal directory**:
```bash
cd /home/uttam/IdeaProjects/aegis-portal
```

2. **Install dependencies** (if not already done):
```bash
npm install
```

3. **Start development server**:
```bash
npm run dev
```

The portal will be available at `http://localhost:5173`

### Features
- Login with backend authentication
- Dashboard with key statistics
- Create and manage registration keys
- View and regenerate keys
- Organization profile management

## Testing the Complete Flow

### 1. Start Backend Services
```bash
# Terminal 1 - Aegis API
cd /home/uttam/IdeaProjects/aegis
./gradlew bootRun

# Terminal 2 - Bank Backend (optional)
cd /home/uttam/IdeaProjects/backend-app
./gradlew bootRun
```

### 2. Start Portal
```bash
# Terminal 3
cd /home/uttam/IdeaProjects/aegis-portal
npm run dev
```

### 3. Login to Portal
1. Open `http://localhost:5173`
2. Use one of the demo credentials:
   - Admin: `admin@aegis.com` / `admin123`
   - Bank: `bank@ucobank.com` / `bank123`
   - Fintech: `fintech@paytm.com` / `fintech123`

### 4. Create Registration Key
1. Navigate to "Registration Keys"
2. Click "Create New Key"
3. Enter Client ID (e.g., "mobile-app-prod")
4. Add description
5. Optionally set expiration date
6. Copy the generated key (shown only once!)

### 5. Use in Android App
The registration key can be used in the Android SDK for device provisioning.

## Architecture Overview

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Aegis Portal   │────▶│   Aegis API     │◀────│  Bank Backend   │
│   (React/MUI)   │     │  (Spring Boot)  │     │  (Spring Boot)  │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                               ▲
                               │
                        ┌──────┴────────┐
                        │  Android App  │
                        │  (With SDK)   │
                        └───────────────┘
```

## Security Features

1. **CORS Protection**: Configured to allow only specific origins
2. **Password Encryption**: BCrypt encryption for user passwords
3. **Session Management**: Stateless JWT-based authentication (basic implementation)
4. **Key Security**: Registration keys shown only once after creation
5. **HMAC Signing**: All API requests from mobile apps are HMAC-signed

## Next Steps

1. **Production Deployment**:
   - Replace simple token with proper JWT implementation
   - Configure HTTPS/TLS
   - Set up proper CORS origins
   - Use environment variables for configuration

2. **Enhanced Features**:
   - Email notifications for key expiration
   - API usage analytics
   - Multi-factor authentication
   - Audit logs

3. **Mobile Integration**:
   - Use generated registration keys in Android app
   - Implement device provisioning flow
   - Test HMAC signature validation

## Troubleshooting

1. **CORS Issues**: Ensure backend is running before starting portal
2. **Login Fails**: Check if backend has initialized demo users
3. **Database Connection**: Ensure PostgreSQL is running on port 5432
4. **Redis Connection**: Ensure Redis is running on port 6379

## Support

For issues or questions:
- Check logs in backend console
- Verify all services are running
- Ensure correct ports are available