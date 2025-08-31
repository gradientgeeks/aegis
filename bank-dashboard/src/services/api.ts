import axios from 'axios';
import type { User, Transaction, Device, AdminUser, BlockDeviceRequest, DashboardStats } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081';
const AEGIS_API_URL = import.meta.env.VITE_AEGIS_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const aegisApi = axios.create({
  baseURL: AEGIS_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('adminToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

aegisApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('adminToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authService = {
  login: async (username: string, password: string): Promise<AdminUser> => {
    const response = await api.post('/api/admin/login', { username, password });
    if (response.data.token) {
      localStorage.setItem('adminToken', response.data.token);
    }
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('adminToken');
  },

  getCurrentAdmin: async (): Promise<AdminUser> => {
    const response = await api.get('/api/admin/profile');
    return response.data;
  },
};

export const userService = {
  getAllUsers: async (): Promise<User[]> => {
    const response = await api.get('/api/admin/users');
    return response.data;
  },

  getUserById: async (userId: number): Promise<User> => {
    const response = await api.get(`/api/admin/users/${userId}`);
    return response.data;
  },

  getUserDevices: async (userId: number): Promise<Device[]> => {
    const response = await api.get(`/api/admin/users/${userId}/devices`);
    return response.data;
  },
};

export const transactionService = {
  getAllTransactions: async (params?: {
    userId?: number;
    deviceId?: string;
    startDate?: string;
    endDate?: string;
    status?: string;
  }): Promise<Transaction[]> => {
    const response = await api.get('/api/admin/transactions', { params });
    return response.data;
  },

  getTransactionById: async (transactionId: number): Promise<Transaction> => {
    const response = await api.get(`/api/admin/transactions/${transactionId}`);
    return response.data;
  },
};

export const deviceService = {
  getAllDevices: async (): Promise<Device[]> => {
    const response = await api.get('/api/admin/devices');
    return response.data;
  },

  getDeviceById: async (deviceId: string): Promise<Device> => {
    const response = await api.get(`/api/admin/devices/${deviceId}`);
    return response.data;
  },

  blockDevice: async (request: BlockDeviceRequest): Promise<void> => {
    await aegisApi.post('/api/fraud/block-device', request);
  },

  unblockDevice: async (deviceId: string): Promise<void> => {
    await aegisApi.post('/api/fraud/unblock-device', { deviceId });
  },
};

export const dashboardService = {
  getStats: async (): Promise<DashboardStats> => {
    const response = await api.get('/api/admin/dashboard/stats');
    return response.data;
  },

  getRecentTransactions: async (limit: number = 10): Promise<Transaction[]> => {
    const response = await api.get(`/api/admin/transactions/recent?limit=${limit}`);
    return response.data;
  },

  getSuspiciousActivities: async (): Promise<any[]> => {
    const response = await api.get('/api/admin/suspicious-activities');
    return response.data;
  },
};