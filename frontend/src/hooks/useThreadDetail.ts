import { useState, useEffect } from 'react';
import mailService from '../services/mailService';
import { Thread } from '../types/mail';

export function useThreadDetail(selectedThreadId: string | null) {
    const [selectedThreadData, setSelectedThreadData] = useState<Thread | null>(null);
    const [isLoadingDetail, setIsLoadingDetail] = useState<boolean>(false);

    useEffect(() => {
        if (!selectedThreadId) {
            setSelectedThreadData(null);
            return;
        }

        const controller = new AbortController();

        const fetchDetail = async () => {
            setIsLoadingDetail(true);
            try {
                const data = await mailService.fetchThreadDetail(selectedThreadId, controller.signal) as Thread;
                setSelectedThreadData(data);
            } catch (error: any) {
                if (error.name === 'CanceledError' || error.name === 'AbortError' || error.message === 'canceled') {
                    return;
                }
                console.error("Failed to fetch thread detail:", error);
            } finally {
                if (!controller.signal.aborted) {
                    setIsLoadingDetail(false);
                }
            }
        };

        fetchDetail();

        return () => {
            controller.abort();
        };
    }, [selectedThreadId]);

    const updateThreadDataLocally = (updatedData: Thread) => {
        const isUnread = updatedData.unread;
        const fixedMessages = updatedData.messages?.map(m => ({ 
            ...m, 
            labelIds: isUnread ? m.labelIds : m.labelIds.filter(l => l !== 'UNREAD') 
        }));
        const fixedData = { ...updatedData, unread: isUnread, messages: fixedMessages };
        setSelectedThreadData(fixedData);
        return fixedData;
    };

    return {
        selectedThreadData,
        isLoadingDetail,
        updateThreadDataLocally,
        clearThreadLocally: () => setSelectedThreadData(null)
    };
}
