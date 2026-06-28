import api from '../api/axiosConfig';

/**
 * Fetch audit logs for a specific incident
 * @param {string|number} incidentId 
 * @returns {Promise<Array>} List of audit logs
 */
export const getAuditLogs = async (incidentId) => {
  const response = await api.get(`/v1/incidents/${incidentId}/audit-logs`);
  return response.data;
};
