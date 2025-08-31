export interface User {
  id: number;
  username: string;
  email: string;
  name: string;
  deviceIds: string[];
  createdAt: string;
  lastLogin?: string;
  status: 'active' | 'blocked';
}

export interface Transaction {
  id: number;
  userId: number;
  username: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  type: 'CREDIT' | 'DEBIT' | 'TRANSFER';
  status: 'PENDING' | 'COMPLETED' | 'FAILED';
  deviceId: string;
  timestamp: string;
  description?: string;
}

export interface Device {
  deviceId: string;
  userId: number;
  username: string;
  status: 'active' | 'blocked';
  lastUsed: string;
  registeredAt: string;
  deviceInfo?: {
    model?: string;
    os?: string;
    version?: string;
  };
}

export interface AdminUser {
  id: number;
  username: string;
  role: 'ADMIN' | 'SUPER_ADMIN';
  token?: string;
}

export interface BlockDeviceRequest {
  deviceId: string;
  reason: string;
  blockedBy: string;
}

export interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  blockedDevices: number;
  todayTransactions: number;
  failedTransactions: number;
  suspiciousActivities: number;
}