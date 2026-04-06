import api from './api';

class AuthService {
    async checkAuthStatus() {
        const response = await api.get('/auth/me');
        return response.data;
    }
}

export default new AuthService();
