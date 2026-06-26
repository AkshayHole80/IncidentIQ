import axios from 'axios';
import { config } from '../config/config';

const api = axios.create({
  baseURL: config.API_BASE_URL, // Directs requests to the API Gateway
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Inject JWT token from localStorage
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Intercept 401 errors for auto-logout
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      localStorage.removeItem('userEmail');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
