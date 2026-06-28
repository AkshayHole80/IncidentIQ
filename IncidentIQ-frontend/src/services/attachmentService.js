import api from '../api/axiosConfig';

/**
 * Fetch all attachments for a specific incident
 * @param {string|number} incidentId 
 * @returns {Promise<Array>} List of attachments
 */
export const getAttachments = async (incidentId) => {
  const response = await api.get(`/v1/incidents/${incidentId}/attachments`);
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

  const response = await api.post(`/v1/incidents/${incidentId}/attachments`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    },
  });
  return response.data;
};

/**
 * Delete a specific attachment
 * @param {string|number} attachmentId 
 * @returns {Promise<void>}
 */
export const deleteAttachment = async (attachmentId) => {
  const response = await api.delete(`/v1/incidents/attachments/${attachmentId}`);
  return response.data;
};

/**
 * Fetch pre-signed View URL for an attachment
 * @param {string|number} attachmentId 
 * @returns {Promise<object>} The URL wrapper object { url: "..." }
 */
export const getAttachmentViewUrl = async (attachmentId) => {
  const response = await api.get(`/v1/incidents/attachments/${attachmentId}/view`);
  return response.data;
};

/**
 * Fetch pre-signed Download URL for an attachment
 * @param {string|number} attachmentId 
 * @returns {Promise<object>} The URL wrapper object { url: "..." }
 */
export const getAttachmentDownloadUrl = async (attachmentId) => {
  const response = await api.get(`/v1/incidents/attachments/${attachmentId}/download`);
  return response.data;
};

