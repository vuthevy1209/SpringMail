import api from './api';

class MailService {
    async fetchEmails(folder: string, category?: string, signal?: AbortSignal) {
        let apiUrl = `/mail/threads?folder=${folder}`;
        if (folder === 'inbox' && category) {
            apiUrl += `&category=${category}`;
        }
        const response = await api.get(apiUrl, { signal });
        return response.data.result;
    }

    async fetchThreadDetail(threadId: string, signal?: AbortSignal) {
        const response = await api.get(`/mail/threads/${threadId}`, { signal });
        return response.data.result;
    }

    async downloadAttachment(messageId: string, attachmentId: string, filename?: string, mimeType?: string) {
        let url = `/mail/attachments?messageId=${messageId}&attachmentId=${attachmentId}`;
        if (filename) url += `&filename=${encodeURIComponent(filename)}`;
        if (mimeType) url += `&mimeType=${encodeURIComponent(mimeType)}`;

        const response = await api.get(url, { responseType: 'blob' });
        const blob = new Blob([response.data]);
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename || 'attachment';
        link.click();
        URL.revokeObjectURL(link.href);
    }

    async sync(signal?: AbortSignal) {
        const response = await api.post('/sync', {}, { signal });
        return response.data.result;
    }
}

export default new MailService();
