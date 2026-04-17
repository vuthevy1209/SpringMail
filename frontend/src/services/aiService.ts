import api from './api';

export interface AiSummaryRequest {
    threadId: string;
    content: string;
}

export interface AiSummaryResponse {
    markdownSummary: string;
}

export const aiService = {
    summarizeEmail: async (threadId: string, content: string): Promise<AiSummaryResponse> => {
        const response = await api.post<AiSummaryResponse>('/api/v1/ai/summarize', {
            threadId,
            content,
        });
        return response.data;
    },
};

export default aiService;