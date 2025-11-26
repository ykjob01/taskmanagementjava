import api from './api';

export const taskService = {
    async getTasks(page = 0, size = 10, sortBy = 'createdAt', status = '') {
        const params = { page, size };
        if (sortBy) params.sortBy = sortBy;
        if (status) params.status = status;

        const response = await api.get('/tasks', { params });
        return response.data;
    },

    async getTaskById(id) {
        const response = await api.get(`/tasks/${id}`);
        return response.data;
    },

    async createTask(taskData) {
        const response = await api.post('/tasks', taskData);
        return response.data;
    },

    async updateTask(id, taskData) {
        const response = await api.put(`/tasks/${id}`, taskData);
        return response.data;
    },

    async deleteTask(id) {
        const response = await api.delete(`/tasks/${id}`);
        return response.data;
    }
};
