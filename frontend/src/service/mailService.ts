import api from './api';

class MailService {
    async fetchEmails(folder: string, category?: string) {
        let apiUrl = `/get-emails?folder=${folder}`;
        if (folder === 'inbox' && category) {
            apiUrl += `&category=${category}`;
        }
        const response = await api.get(apiUrl);
        return response.data.result;
    }
}

export default new MailService();
