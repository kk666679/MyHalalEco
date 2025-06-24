import axios from 'axios';

// API base URL from environment variable, fallback to default
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:5000/api';

// Create axios instance with default config
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Required for cookies/authentication
});

// Request interceptor for adding auth token
apiClient.interceptors.request.use(
  (config) => {
    // Get token from cookie if exists
    const token = document.cookie
      .split('; ')
      .find((row) => row.startsWith('auth-token='))
      ?.split('=')[1];

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for handling errors
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized error (e.g., redirect to login)
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// API endpoints
export const endpoints = {
  auth: {
    login: '/auth/login',
    logout: '/auth/logout',
    me: '/auth/me',
    register: '/auth/register',
  },
  vendor: {
    list: '/vendors',
    details: (id: string) => `/vendors/${id}`,
    create: '/vendors',
    update: (id: string) => `/vendors/${id}`,
    delete: (id: string) => `/vendors/${id}`,
  },
  // Add other endpoints as needed
};

// Type for API response
export interface ApiResponse<T = unknown> {
  success: boolean;
  message?: string;
  data?: T;
}

// Export methods
export const api = {
  get: <T>(url: string) => 
    apiClient.get<ApiResponse<T>>(url).then(response => response.data),
  
  post: <T>(url: string, data?: Record<string, unknown>) =>
    apiClient.post<ApiResponse<T>>(url, data).then(response => response.data),
  
  put: <T>(url: string, data?: Record<string, unknown>) =>
    apiClient.put<ApiResponse<T>>(url, data).then(response => response.data),
  
  delete: <T>(url: string) =>
    apiClient.delete<ApiResponse<T>>(url).then(response => response.data),
};

export default api;
