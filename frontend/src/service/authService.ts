import api from './api';

class AuthService {
    async checkAuthStatus() {
        try {
            const response = await api.get('/auth/me');
            if (response.data.result) {
                localStorage.setItem('user', JSON.stringify(response.data.result));
            }
            return response.data.result;
        } catch (error) {
            localStorage.removeItem('user');
            throw error;
        }
    }

    async logout() {
        try {
            await api.post('/auth/logout');
        } finally {
            localStorage.removeItem('user');
        }
    }
}

export default new AuthService();
