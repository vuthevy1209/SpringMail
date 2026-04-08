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

    async fetchThreadDetail(threadId: string) {
        const response = await api.get(`/get-thread/${threadId}`);
        return response.data.result;
    }
}

export default new MailService();
