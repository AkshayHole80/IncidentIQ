import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '../api/axiosConfig';

const AuthContext = createContext(null);

// Helper to decode JWT without external packages
const parseJwt = (token) => {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch (e) {
    return null;
  }
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchUserProfile = async (token, role) => {
    try {
      // 1. Try to call the specified GET /api/v1/users/me
      const meResponse = await api.get('/v1/users/me');
      setUser(meResponse.data);
      localStorage.setItem('role', meResponse.data.role);
      localStorage.setItem('userEmail', meResponse.data.email);
    } catch (error) {
      console.warn("GET /api/v1/users/me failed or is not implemented. Falling back to JWT decode & email profile lookup.", error);
      
      // 2. Fallback: Parse email from JWT and fetch profile by email
      const decoded = parseJwt(token);
      if (decoded && decoded.sub) {
        try {
          const emailResponse = await api.get(`/v1/users/email/${decoded.sub}`);
          setUser(emailResponse.data);
          localStorage.setItem('role', emailResponse.data.role);
          localStorage.setItem('userEmail', emailResponse.data.email);
        } catch (fallbackError) {
          console.error("Fallback user lookup failed", fallbackError);
          // 3. Last resort: construct user from JWT claims
          const minimalUser = {
            email: decoded.sub,
            role: role || localStorage.getItem('role') || 'USER',
            firstName: 'User',
            lastName: ''
          };
          setUser(minimalUser);
        }
      } else {
        // No valid token payload
        logout();
      }
    }
  };

  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('token');
      const role = localStorage.getItem('role');
      if (token) {
        await fetchUserProfile(token, role);
      }
      setLoading(false);
    };
    initializeAuth();
  }, []);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const response = await api.post('/v1/auth/login', { email, password });
      const { token, role } = response.data;
      
      localStorage.setItem('token', token);
      localStorage.setItem('role', role);

      await fetchUserProfile(token, role);
      return { success: true, role };
    } catch (error) {
      console.error("Login failed", error);
      setLoading(false);
      throw error;
    }
  };

  const register = async (registerData) => {
    setLoading(true);
    try {
      const response = await api.post('/v1/auth/register', registerData);
      const { token, role } = response.data;
      
      localStorage.setItem('token', token);
      localStorage.setItem('role', role);

      await fetchUserProfile(token, role);
      return { success: true, role };
    } catch (error) {
      console.error("Registration failed", error);
      setLoading(false);
      throw error;
    }
  };

  const [darkMode, setDarkMode] = useState(() => localStorage.getItem('darkMode') === 'true');

  const toggleDarkMode = () => {
    setDarkMode(prev => {
      const nextValue = !prev;
      localStorage.setItem('darkMode', String(nextValue));
      if (nextValue) {
        document.body.classList.add('dark', 'dark-mode');
      } else {
        document.body.classList.remove('dark', 'dark-mode');
      }
      return nextValue;
    });
  };

  useEffect(() => {
    // Initial sync with classList
    if (darkMode) {
      document.body.classList.add('dark', 'dark-mode');
    } else {
      document.body.classList.remove('dark', 'dark-mode');
    }
  }, [darkMode]);

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('userEmail');
    setUser(null);
  };

  const value = {
    user,
    loading,
    login,
    register,
    logout,
    isAuthenticated: !!user,
    darkMode,
    toggleDarkMode,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
