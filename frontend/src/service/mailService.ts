import api from './api';

class MailService {
    async fetchEmails(folder: string, category?: string, signal?: AbortSignal) {
        let apiUrl = `/get-emails?folder=${folder}`;
        if (folder === 'inbox' && category) {
            apiUrl += `&category=${category}`;
        }
        const response = await api.get(apiUrl, { signal });
        return response.data.result;
    }

    async fetchThreadDetail(threadId: string, signal?: AbortSignal) {
        const response = await api.get(`/get-thread/${threadId}`, { signal });
        return response.data.result;
    }

    async sync(signal?: AbortSignal) {
        const response = await api.post('/sync', {}, { signal });
        return response.data.result;
    }
}

export default new MailService();
