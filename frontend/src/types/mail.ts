export interface Label {
    id: string;
    name: string;
    type: 'system' | 'user';
    messageListVisibility?: 'show' | 'hide';
    labelListVisibility?: 'labelShow' | 'labelHide';
}

export interface MessageHeader {
    name: string;
    value: string;
}

export interface MessagePart {
    partId: string;
    mimeType: string;
    filename: string;
    headers: MessageHeader[];
    body: {
        size: number;
        data?: string;
    };
    parts?: MessagePart[];
}

export interface Message {
    id: string;
    threadId: string;
    labelIds: string[];
    snippet: string;
    internalDate: string;
    payload: MessagePart;
    sizeEstimate: number;
    raw?: string;
}

export interface Thread {
    id: string;
    snippet: string;
    historyId: string;
    messages?: Message[];
    unread?: boolean;
    lastMessageTimestamp?: string;
}

export interface ThreadPageResponse {
    content: Thread[];
    last: boolean;
    pageNo: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
}
