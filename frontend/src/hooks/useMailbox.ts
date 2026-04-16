import { useState, useEffect, useRef, useCallback } from 'react';
import mailService from '../services/mailService';
import { useAuth } from '../context/AuthContext';
import { Thread } from '../types/mail';

export function useMailbox(folder: string, searchQuery: string = '') {
    const [threads, setThreads] = useState<Thread[]>([]);
    const [isLoadingThreads, setIsLoadingThreads] = useState<boolean>(false);
    
    const [dbPage, setDbPage] = useState<number>(0);
    const [hasMoreLocal, setHasMoreLocal] = useState<boolean>(true);
    const [googlePageToken, setGooglePageToken] = useState<string | null>(null);
    const [hasMoreGoogle, setHasMoreGoogle] = useState<boolean>(true);
    const [isLoadingMore, setIsLoadingMore] = useState<boolean>(false);
    
    const [activeTab, setActiveTab] = useState<string>('primary');
    const [showUnreadOnly, setShowUnreadOnly] = useState<boolean>(false);

    const fetchControllerRef = useRef<AbortController | null>(null);
    const { user } = useAuth() as any;

    const getLabelIds = useCallback((): string[] => {
        if (folder === 'all') return [];
        if (folder === 'sent') return ['SENT'];
        if (folder === 'drafts') return ['DRAFT'];
        if (folder === 'trash') return ['TRASH'];
        if (folder === 'starred') return ['STARRED'];
        if (folder === 'important') return ['IMPORTANT'];
        if (folder === 'spam') return ['SPAM'];
        if (folder === 'inbox') {
            const tabs: Record<string, string> = {
                primary: 'CATEGORY_PERSONAL',
                social: 'CATEGORY_SOCIAL',
                promotions: 'CATEGORY_PROMOTIONS',
                updates: 'CATEGORY_UPDATES',
                forums: 'CATEGORY_FORUMS'
            };
            return ['INBOX', tabs[activeTab] || 'CATEGORY_PERSONAL'];
        }
        return ['INBOX'];
    }, [folder, activeTab]);

    useEffect(() => {
        if (fetchControllerRef.current) {
            fetchControllerRef.current.abort();
        }
        const controller = new AbortController();
        fetchControllerRef.current = controller;

        const fetchLatestEmails = async () => {
            if (user?.syncStatus === 'INITIAL_SYNC_IN_PROGRESS') return;

            setThreads([]);
            setIsLoadingThreads(true);
            setShowUnreadOnly(false);
            setIsLoadingMore(false);

            setDbPage(0);
            setHasMoreLocal(true);
            setGooglePageToken(null);
            setHasMoreGoogle(true);

            try {
                if (searchQuery.trim()) {
                    // Perform search
                    const data = await mailService.searchEmails(searchQuery.trim(), 0, 20, controller.signal) as any;
                    const fetchedThreads: Thread[] = data.content || [];
                    if (!controller.signal.aborted) {
                        setThreads(fetchedThreads);
                        setHasMoreLocal(!data.last);
                        setHasMoreGoogle(false); // Disable google fetching for search
                    }
                } else {
                    const labelIds = getLabelIds();
                    let data = await mailService.fetchEmails(labelIds, 0, 20, controller.signal) as any;
                    let fetchedThreads: Thread[] = data.content || [];
                    let localHasMore = !data.last;

                    if (fetchedThreads.length === 0) {
                        try {
                            const googleData = await mailService.fetchOlderFromGoogle(labelIds, null, null, controller.signal) as any;
                            if (!controller.signal.aborted) {
                                setGooglePageToken(googleData.nextPageToken);
                                setHasMoreGoogle(!!googleData.nextPageToken);
                            }
                            
                            data = await mailService.fetchEmails(labelIds, 0, 20, controller.signal) as any;
                            fetchedThreads = data.content || [];
                            localHasMore = !data.last;
                        } catch (e: any) {
                             if (e.name === 'CanceledError' || e.name === 'AbortError' || e.message === 'canceled') return;
                             console.error("Auto fetch older failed:", e);
                        }
                    }

                    if (!controller.signal.aborted) {
                        setThreads(fetchedThreads);
                        setHasMoreLocal(localHasMore);
                    }
                }
            } catch (error: any) {
                if (error.name === 'CanceledError' || error.name === 'AbortError' || error.message === 'canceled') {
                    return;
                }
                console.error("Failed to fetch emails:", error);
            } finally {
                if (!controller.signal.aborted) {
                    setIsLoadingThreads(false);
                }
            }
        };

        fetchLatestEmails();

        return () => {
            controller.abort();
        };
    }, [folder, activeTab, user?.syncStatus, getLabelIds, searchQuery]);

    const loadMore = async () => {
        if (isLoadingMore) return;
        setIsLoadingMore(true);
        const signal = fetchControllerRef.current?.signal;

        try {
            if (searchQuery.trim()) {
                if (hasMoreLocal) {
                    const nextDbPage = Math.floor(threads.length / 20);
                    const data = await mailService.searchEmails(searchQuery.trim(), nextDbPage, 20, signal) as any;
                    
                    if (signal?.aborted) return;
                    setThreads((prev: Thread[]) => {
                        const existingIds = new Set(prev.map(t => t.id));
                        const newItems = (data.content || []).filter((t: Thread) => !existingIds.has(t.id));
                        return [...prev, ...newItems];
                    });
                    setDbPage(nextDbPage);
                    setHasMoreLocal(!data.last);
                }
            } else {
                const labelIds = getLabelIds();

                if (hasMoreLocal) {
                    const nextDbPage = Math.floor(threads.length / 20);
                    const data = await mailService.fetchEmails(labelIds, nextDbPage, 20, signal) as any;
                    
                    if (signal?.aborted) return;
                    setThreads((prev: Thread[]) => {
                    const existingIds = new Set(prev.map(t => t.id));
                    const newItems = (data.content || []).filter((t: Thread) => !existingIds.has(t.id));
                    return [...prev, ...newItems];
                });
                setDbPage(nextDbPage);
                setHasMoreLocal(!data.last);
                } else if (hasMoreGoogle) {
                    const oldestThread = threads.length > 0 ? threads[threads.length - 1] : null;
                    const beforeTimestamp = oldestThread ? oldestThread.lastMessageTimestamp : null;

                    const googleData = await mailService.fetchOlderFromGoogle(labelIds, googlePageToken, beforeTimestamp, signal) as any;
                    
                    if (signal?.aborted) return;
                    setGooglePageToken(googleData.nextPageToken);
                    if (!googleData.nextPageToken) {
                        setHasMoreGoogle(false);
                    }

                    const currentDbPage = Math.floor(threads.length / 20);
                    const data = await mailService.fetchEmails(labelIds, currentDbPage, 20, signal) as any;
                    
                    if (signal?.aborted) return;
                    setThreads((prev: Thread[]) => {
                        const existingIds = new Set(prev.map(t => t.id));
                        const newItems = (data.content || []).filter((t: Thread) => !existingIds.has(t.id));
                        return [...prev, ...newItems];
                    });
                    
                    setDbPage(currentDbPage);
                    setHasMoreLocal(!data.last);
                }
            }
        } catch (error: any) {
            if (error.name === 'CanceledError' || error.name === 'AbortError' || error.message === 'canceled') return;
            console.error("Failed to load more emails:", error);
        } finally {
            if (!signal?.aborted) {
                setIsLoadingMore(false);
            }
        }
    };

    const displayThreads = showUnreadOnly 
        ? threads.filter(t => t.unread) 
        : threads;

    const updateThreadLocally = (updatedData: Thread) => {
        setThreads(prevThreads => prevThreads.map((t: Thread) => (t.id === updatedData.id ? { ...t, ...updatedData } : t)));
    };

    const removeThreadLocally = (deletedId: string) => {
        setThreads(prevThreads => prevThreads.filter((t: Thread) => t.id !== deletedId));
    };

    return {
        threads: displayThreads,
        isLoadingThreads,
        activeTab,
        setActiveTab,
        showUnreadOnly,
        setShowUnreadOnly,
        hasMore: hasMoreLocal || hasMoreGoogle,
        isLoadingMore,
        loadMore,
        updateThreadLocally,
        removeThreadLocally
    };
}
