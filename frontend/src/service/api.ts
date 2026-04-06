import axios from 'axios';
import toast from 'react-hot-toast';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    withCredentials: true,
});

// Request interceptor
api.interceptors.request.use(
    (config) => {
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor
api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response) {
            const status = error.response.status;
            if (status === 401) {
                console.warn('Unauthorized request, session might have expired.');
                window.location.href = '/login';
            }
            toast.error(error.response.data?.message || `An error occurred (${status})`);
        } else {
            toast.error(error.message || 'Network error occurred');
        }
        return Promise.reject(error);
    }
);

export default api;
