# Aegis Portal

A comprehensive management portal for banks and fintech organizations to manage their Aegis Security API registration keys and client IDs.

## Features

- **Authentication**: Secure login system for organizations
- **Dashboard**: Overview of registration keys with statistics
- **Registration Key Management**: 
  - Create new registration keys
  - View and manage existing keys
  - Regenerate or revoke keys
  - Set expiration dates
- **Organization Profile**: Manage organization details and API access information
- **Responsive Design**: Works seamlessly on desktop and mobile devices

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Aegis Backend API running on `http://localhost:8080/api`

### Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

The portal will be available at `http://localhost:5173`

### Demo Credentials

For testing purposes, use these credentials:
- Email: `admin@aegis.com`
- Password: `admin123`

## Tech Stack

- **React 19** with TypeScript
- **Material-UI (MUI)** for UI components
- **React Router** for navigation
- **Axios** for API calls
- **Vite** for fast development and building
- **date-fns** for date manipulation

## Project Structure

```
src/
├── components/       # Reusable components
│   ├── Layout.tsx   # Main layout with navigation
│   └── PrivateRoute.tsx
├── pages/           # Page components
│   ├── Login.tsx
│   ├── Dashboard.tsx
│   ├── RegistrationKeys.tsx
│   ├── CreateKey.tsx
│   └── Profile.tsx
├── services/        # API services
│   ├── api.ts      # Aegis API client
│   └── auth.ts     # Authentication service
├── types/          # TypeScript type definitions
└── App.tsx         # Main app component
```

## API Integration

The portal integrates with the Aegis Backend API endpoints:

- `POST /admin/registration-keys` - Create new registration key
- `GET /admin/registration-keys` - List all registration keys
- `GET /admin/registration-keys/{clientId}` - Get specific key
- `PUT /admin/registration-keys/{clientId}/revoke` - Revoke a key
- `PUT /admin/registration-keys/{clientId}/regenerate` - Regenerate a key

## Security Features

- JWT-based authentication (mock implementation - replace with actual)
- Secure key display with masking
- One-time key display on creation
- Automatic session management

## Build for Production

```bash
npm run build
```

The production-ready files will be in the `dist/` directory.

## Future Enhancements

- Real authentication integration with backend
- API usage analytics and monitoring
- Multi-organization support
- Role-based access control
- Audit logs for key operations
- Email notifications for key expiration
- API documentation integration