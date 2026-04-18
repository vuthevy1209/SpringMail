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

export interface EventDto {
    title: string;
    datetime: string;
    location: string;
    description: string;
    status: string;
}

export interface UpcomingEventsResponse {
    events: EventDto[];
    rawAnalysis: string;
}

export const aiService = {
    summarizeEmail: async (threadId: string, content: string): Promise<AiSummaryResponse> => {
        const response = await api.post<AiSummaryResponse>('/ai/summarize', {
            threadId,
            content,
        });
        return response.data;
    },
    generateDraft: async (threadId: string, content: string, format: string): Promise<AiDraftResponse> => {
        const response = await api.post<AiDraftResponse>('/ai/draft', {
            threadId,
            content,
            format,
        });
        return response.data;
    },
    extractUpcomingEvents: async (): Promise<UpcomingEventsResponse> => {
        const response = await api.get<UpcomingEventsResponse>('/ai/upcoming-events');
        return response.data;
    }
};

export default aiService;