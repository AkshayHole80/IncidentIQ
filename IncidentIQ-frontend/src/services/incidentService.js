import api from '../api/axiosConfig';

/**
 * Update an existing incident (Only for OPEN status and if current user is creator)
 * @param {string|number} id 
 * @param {object} payload - { title, description }
 * @returns {Promise<object>} updated incident DTO
 */
export const updateIncident = async (id, payload) => {
  const response = await api.put(`/api/v1/incidents/${id}`, payload);
  return response.data;
};

/**
 * Delete an existing incident (Only for OPEN status and if current user is creator)
 * @param {string|number} id 
 * @returns {Promise<void>}
 */
export const deleteIncident = async (id) => {
  const response = await api.delete(`/api/v1/incidents/${id}`);
  return response.data;
};
