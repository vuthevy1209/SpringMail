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
        if (axios.isCancel(error)) {
            return Promise.reject(error);
        }

        if (error.response) {
            const status = error.response.status;
            
            // Redirect to login if user gets a Gmail unauthenticated error or 401
            if (status === 401) {
                if (error.config.url?.includes('/auth/me')) {
                    return Promise.reject(error);
                }
                
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
                return Promise.reject(error);
            }

            toast.error(error.response.data?.message || `An error occurred (${status})`);
        } else {
            toast.error(error.message || 'Network error occurred');
        }
        return Promise.reject(error);
    }
);

export default api;
