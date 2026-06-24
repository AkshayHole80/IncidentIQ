import api from '../api/axiosConfig';

/**
 * Fetch all attachments for a specific incident
 * @param {string|number} incidentId 
 * @returns {Promise<Array>} List of attachments
 */
export const getAttachments = async (incidentId) => {
  const response = await api.get(`/api/v1/incidents/${incidentId}/attachments`);
  return response.data;
};

/**
 * Upload a new attachment for a specific incident
 * @param {string|number} incidentId 
 * @param {File} file 
 * @returns {Promise<object>} Uploaded attachment details
 */
export const uploadAttachment = async (incidentId, file) => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await api.post(`/api/v1/incidents/${incidentId}/attachments`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
};
