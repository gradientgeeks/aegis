import { type LoginCredentials, type AuthState } from '../types';
import axios from 'axios';

const AUTH_API_URL = 'http://localhost:8080/api/auth';

export const authService = {
  login: async (credentials: LoginCredentials): Promise<AuthState> => {
    try {
      const response = await axios.post(`${AUTH_API_URL}/login`, credentials);
      const data = response.data;
      
      const authState: AuthState = {
        isAuthenticated: true,
        user: {
          id: data.id.toString(),
          name: data.name,
          email: data.email,
          organization: data.organization,
          role: data.role,
        },
        token: data.token,
      };
      
      localStorage.setItem('authToken', authState.token!);
      localStorage.setItem('user', JSON.stringify(authState.user));
      localStorage.setItem('userOrganization', authState.user!.organization);
      
      return authState;
    } catch (error: any) {
      if (error.response?.status === 401 && error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status === 401) {
        throw new Error('Invalid email or password');
      }
      throw new Error('Login failed. Please try again.');
    }
  },

  logout: async (): Promise<void> => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    localStorage.removeItem('userOrganization');
  },

  getCurrentUser: (): AuthState => {
    const token = localStorage.getItem('authToken');
    const userStr = localStorage.getItem('user');
    
    if (token && userStr) {
      return {
        isAuthenticated: true,
        user: JSON.parse(userStr),
        token,
      };
    }
    
    return {
      isAuthenticated: false,
    };
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('authToken');
  },
};