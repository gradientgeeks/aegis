import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import RegistrationKeys from './pages/RegistrationKeys';
import CreateKey from './pages/CreateKey';
import Profile from './pages/Profile';
import OrganizationApproval from './pages/OrganizationApproval';
import DeviceManagement from './pages/DeviceManagement';
import FraudDashboard from './pages/FraudDashboard';
import PolicyManagement from './pages/PolicyManagement';
import PolicyForm from './pages/PolicyForm';

// Components
import Layout from './components/Layout';
import PrivateRoute from './components/PrivateRoute';

// Theme
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
      light: '#42a5f5',
      dark: '#1565c0',
    },
    secondary: {
      main: '#dc004e',
      light: '#e33371',
      dark: '#9a0036',
    },
    background: {
      default: '#f5f5f5',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h4: {
      fontWeight: 600,
    },
    h5: {
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 8,
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          
          <Route element={<PrivateRoute />}>
            <Route element={<Layout />}>
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/registration-keys" element={<RegistrationKeys />} />
              <Route path="/registration-keys/create" element={<CreateKey />} />
              <Route path="/device-management" element={<DeviceManagement />} />
              <Route path="/fraud-dashboard" element={<FraudDashboard />} />
              <Route path="/policies" element={<PolicyManagement />} />
              <Route path="/policies/create" element={<PolicyForm />} />
              <Route path="/policies/:id/edit" element={<PolicyForm />} />
              <Route path="/organizations" element={<OrganizationApproval />} />
              <Route path="/profile" element={<Profile />} />
            </Route>
          </Route>
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;