import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useAuth } from './AuthContext';
import { getNotifications, getUnreadCount, markAsRead } from '../services/notificationService';

const NotificationContext = createContext(null);

/**
 * Format timestamp into standard relative format (Just now, 5 minutes ago, etc.)
 * @param {string|Date} dateString 
 * @returns {string} Relative time string
 */
export const formatRelativeTime = (dateString) => {
  if (!dateString) return '';
  const now = new Date();
  const date = new Date(dateString);
  const diffMs = now - date;
  
  if (isNaN(diffMs) || diffMs < 0) return 'Just now';

  const diffSecs = Math.floor(diffMs / 1000);
  if (diffSecs < 60) return 'Just now';

  const diffMins = Math.floor(diffSecs / 60);
  if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;

  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;

  const diffDays = Math.floor(diffHours / 24);
  if (diffDays === 1) return 'Yesterday';
  if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;

  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
};

export const NotificationProvider = ({ children }) => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [isDrawerOpen, setDrawerOpen] = useState(false);

  // Fetch unread count from service
  const refreshUnreadCount = useCallback(async () => {
    if (!user?.id) return;
    try {
      const count = await getUnreadCount(user.id);
      setUnreadCount(count);
    } catch (err) {
      console.warn('Failed to refresh unread count', err);
    }
  }, [user?.id]);

  // Fetch full notifications list from service
  const loadNotifications = useCallback(async () => {
    if (!user?.id) return;
    setLoading(true);
    try {
      const data = await getNotifications(user.id);
      // Sort by date/timestamp desc
      const sortedData = data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setNotifications(sortedData);
      
      // Calculate/refresh unread count
      const count = sortedData.filter(n => !n.read).length;
      setUnreadCount(count);
    } catch (err) {
      console.error('Failed to load notifications', err);
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  // Mark a specific notification as read
  const markNotificationAsRead = useCallback(async (notificationId) => {
    try {
      await markAsRead(notificationId);
      
      // Update local state immediately
      setNotifications(prev => 
        prev.map(n => n.id === notificationId ? { ...n, read: true } : n)
      );
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Failed to mark notification as read', err);
    }
  }, []);

  // Fetch initial data when user changes
  useEffect(() => {
    if (user?.id) {
      loadNotifications();
    } else {
      setNotifications([]);
      setUnreadCount(0);
      setDrawerOpen(false);
    }
  }, [user?.id, loadNotifications]);

  // Set up 30-second polling for unread count
  useEffect(() => {
    if (!user?.id) return;

    const interval = setInterval(() => {
      refreshUnreadCount();
      // If the drawer is currently open, refresh the notifications list as well
      if (isDrawerOpen) {
        loadNotifications();
      }
    }, 30000); // 30 seconds

    return () => clearInterval(interval);
  }, [user?.id, isDrawerOpen, refreshUnreadCount, loadNotifications]);

  const value = {
    notifications,
    unreadCount,
    loading,
    isDrawerOpen,
    setDrawerOpen,
    loadNotifications,
    markAsRead: markNotificationAsRead,
    refreshUnreadCount,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }
  return context;
};
