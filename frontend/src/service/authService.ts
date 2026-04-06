import api from './api';

class AuthService {
    async checkAuthStatus() {
        const response = await api.get('/api/auth/me');
        return response.data;
    }
}

export default new AuthService();
