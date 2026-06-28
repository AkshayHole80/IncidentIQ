import api from '../api/axiosConfig';

/**
 * Update an existing incident (Only for OPEN status and if current user is creator)
 * @param {string|number} id 
 * @param {object} payload - { title, description }
 * @returns {Promise<object>} updated incident DTO
 */
export const updateIncident = async (id, payload) => {
  const response = await api.put(`/v1/incidents/${id}`, payload);
  return response.data;
};

/**
 * Delete an existing incident (Only for OPEN status and if current user is creator)
 * @param {string|number} id 
 * @returns {Promise<void>}
 */
export const deleteIncident = async (id) => {
  const response = await api.delete(`/v1/incidents/${id}`);
  return response.data;
};

/**
 * Sorts incidents by custom lifecycle rule:
 * 1. Status: OPEN > IN_PROGRESS > RESOLVED > CLOSED
 * 2. Priority (if OPEN): CRITICAL > HIGH > MEDIUM > LOW
 * 3. Date: Newest first (createdAt descending)
 * @param {Array} list - The list of incidents to sort
 * @returns {Array} Sorted copy of the list
 */
export const sortIncidents = (list) => {
  if (!list || !Array.isArray(list)) return [];

  const statusWeight = {
    'OPEN': 1,
    'IN_PROGRESS': 2,
    'RESOLVED': 3,
    'CLOSED': 4
  };

  const priorityWeight = {
    'CRITICAL': 1,
    'HIGH': 2,
    'MEDIUM': 3,
    'LOW': 4
  };

  return [...list].sort((a, b) => {
    const statusA = statusWeight[a.status] || 99;
    const statusB = statusWeight[b.status] || 99;

    // 1. Primary Sort: Status
    if (statusA !== statusB) {
      return statusA - statusB;
    }

    // Same status.
    // 2. Secondary Sort: Priority for OPEN status
    if (a.status === 'OPEN') {
      const prioA = priorityWeight[a.priority] || 99;
      const prioB = priorityWeight[b.priority] || 99;
      if (prioA !== prioB) {
        return prioA - prioB;
      }
    }

    // 3. Tertiary Sort: Date (Newest first)
    const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
    const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
    return dateB - dateA;
  });
};

