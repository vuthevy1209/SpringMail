import api from './api';

export interface AiSummaryRequest {
    threadId: string;
    content: string;
}

export interface AiSummaryResponse {
    markdownSummary: string;
}

export interface AiDraftRequest {
    threadId: string;
    content: string;
    format: string;
}

export interface AiDraftResponse {
    draftContent: string;
}

export const aiService = {
    summarizeEmail: async (threadId: string, content: string): Promise<AiSummaryResponse> => {
        const response = await api.post<AiSummaryResponse>('/api/v1/ai/summarize', {
            threadId,
            content,
        });
        return response.data;
    },
    generateDraft: async (threadId: string, content: string, format: string): Promise<AiDraftResponse> => {
        const response = await api.post<AiDraftResponse>('/api/v1/ai/draft', {
            threadId,
            content,
            format,
        });
        return response.data;
    }
};

export default aiService;