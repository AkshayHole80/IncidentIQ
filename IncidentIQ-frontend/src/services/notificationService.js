import api from '../api/axiosConfig';

/**
 * Fetch all notifications for a specific user
 * @param {string|number} userId 
 * @returns {Promise<Array>} List of notifications
 */
export const getNotifications = async (userId) => {
  const response = await api.get(`/api/v1/notifications/user/${userId}`);
  return response.data;
};

/**
 * Fetch unread notification count for a specific user
 * @param {string|number} userId 
 * @returns {Promise<number>} Unread count
 */
export const getUnreadCount = async (userId) => {
  const response = await api.get(`/api/v1/notifications/user/${userId}/unread-count`);
  // Resilient handling in case backend returns number or { count: X }
  if (typeof response.data === 'number') {
    return response.data;
  }
  return response.data?.count ?? response.data?.unreadCount ?? 0;
};

/**
 * Mark a specific notification as read
 * @param {string|number} notificationId 
 * @returns {Promise<object>} updated notification DTO
 */
export const markAsRead = async (notificationId) => {
  const response = await api.patch(`/api/v1/notifications/${notificationId}/read`);
  return response.data;
};
