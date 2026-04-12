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

    async fetchOlderFromGoogle(labelIds: string[], pageToken?: string | null, beforeTimestamp?: number | null, signal?: AbortSignal) {
        const response = await api.post('/mail/sync/fetch-older', { labelIds, pageToken, beforeTimestamp, maxResults: 50 }, { signal });
        return response.data.result;
    }

    async modifyThread(threadId: string, addLabelIds: string[], removeLabelIds: string[]) {
        const response = await api.post(`/mail/threads/${threadId}/modify`, { addLabelIds, removeLabelIds });
        return response.data.result;
    }

    async trashThread(threadId: string) {
        const response = await api.delete(`/mail/threads/${threadId}/trash`);
        return response.data;
    }

    async sendEmail(payload: { to: string, cc?: string, bcc?: string, subject: string, body: string, threadId?: string, inReplyTo?: string, attachments?: File[] }, onUploadProgress?: (progressEvent: any) => void) {
        const formData = new FormData();
        formData.append('to', payload.to);
        if (payload.cc) formData.append('cc', payload.cc);
        if (payload.bcc) formData.append('bcc', payload.bcc);
        formData.append('subject', payload.subject);
        formData.append('body', payload.body);
        if (payload.threadId) formData.append('threadId', payload.threadId);
        if (payload.inReplyTo) formData.append('inReplyTo', payload.inReplyTo);
        
        if (payload.attachments) {
            payload.attachments.forEach(file => {
                formData.append('attachments', file);
            });
        }

        const response = await api.post('/mail/send', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            onUploadProgress
        });
        return response.data;
    }
}

export default new MailService();
