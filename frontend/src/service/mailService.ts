import api from './api';

class MailService {
    async fetchEmails(labelIds: string[], page: number = 0, size: number = 10, signal?: AbortSignal) {
        const response = await api.post(`/mail/threads?page=${page}&size=${size}`, { labelIds }, { signal });
        return response.data.result;
    }

    async fetchThreadDetail(threadId: string, signal?: AbortSignal) {
        const response = await api.get(`/mail/threads/${threadId}`, { signal });
        return response.data.result;
    }

    async downloadAttachment(messageId: string, attachmentId: string, filename?: string, mimeType?: string) {
        const payload = { messageId, attachmentId, filename, mimeType };
        const response = await api.post('/mail/attachments', payload, { responseType: 'blob' });
        
        const blob = new Blob([response.data], { type: mimeType || 'application/octet-stream' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename || 'attachment';
        link.click();
        URL.revokeObjectURL(link.href);
    }
}

export default new MailService();
