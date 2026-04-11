import React, { useState, useEffect, useRef } from 'react';
import { Navigate } from 'react-router-dom';
import authService from '../service/authService';
import mailService from '../service/mailService';
import InboxList from '../components/emails/InboxList';
import EmailReader from '../components/emails/EmailReader';

/**
 * Generic mailbox page shared by all folder routes:
 *   /inbox   → folder="inbox"
 *   /sent    → folder="sent"
 *   /drafts  → folder="drafts"
 *   /trash   → folder="trash"
 */
export default function MailboxPage({ folder }) {
	const [threads, setThreads] = useState([]);
	const [isLoadingThreads, setIsLoadingThreads] = useState(false);
	const [selectedThreadId, setSelectedThreadId] = useState(null);
	const [selectedThreadData, setSelectedThreadData] = useState(null);
	const [isLoadingDetail, setIsLoadingDetail] = useState(false);

	// Pagination & Load More States
	const [dbPage, setDbPage] = useState(0);
	const [hasMoreLocal, setHasMoreLocal] = useState(true);
	const [googlePageToken, setGooglePageToken] = useState(null);
	const [hasMoreGoogle, setHasMoreGoogle] = useState(true);
	const [isLoadingMore, setIsLoadingMore] = useState(false);

	// For inbox only — which category tab is active
	const [activeTab, setActiveTab] = useState('primary');

	// Filter unread only
	const [showUnreadOnly, setShowUnreadOnly] = useState(false);

	const fetchControllerRef = useRef(null);



    const getLabelIds = () => {
        if (folder === 'sent') return ['SENT'];
        if (folder === 'drafts') return ['DRAFT'];
        if (folder === 'trash') return ['TRASH'];
        if (folder === 'starred') return ['STARRED'];
        if (folder === 'important') return ['IMPORTANT'];
        if (folder === 'spam') return ['SPAM'];
        if (folder === 'inbox') {
            const tabs = {
                primary: 'CATEGORY_PERSONAL',
                social: 'CATEGORY_SOCIAL',
                promotions: 'CATEGORY_PROMOTIONS',
                updates: 'CATEGORY_UPDATES',
                forums: 'CATEGORY_FORUMS'
            };
            return ['INBOX', tabs[activeTab] || 'CATEGORY_PERSONAL'];
        }
        return ['INBOX'];
    };

    // Fetch emails whenever folder changes or inbox tab changes
    useEffect(() => {
        if (fetchControllerRef.current) {
            fetchControllerRef.current.abort();
        }
        const controller = new AbortController();
        fetchControllerRef.current = controller;

        const fetchLatestEmails = async () => {
            setThreads([]);
            setSelectedThreadId(null);
            setSelectedThreadData(null);
            setIsLoadingThreads(true);
            setShowUnreadOnly(false); // Reset filter when switching folders
            setIsLoadingMore(false);

            setDbPage(0);
            setHasMoreLocal(true);
            setGooglePageToken(null);
            setHasMoreGoogle(true);

            try {
                const labelIds = getLabelIds();
                let data = await mailService.fetchEmails(labelIds, 0, 20, controller.signal);
                let fetchedThreads = data.content || [];
                let localHasMore = !data.last;

                if (fetchedThreads.length === 0) {
                    try {
                        const googleData = await mailService.fetchOlderFromGoogle(labelIds, null, null, controller.signal);
                        if (!controller.signal.aborted) {
                            setGooglePageToken(googleData.nextPageToken);
                            setHasMoreGoogle(!!googleData.nextPageToken);
                        }
                        
                        data = await mailService.fetchEmails(labelIds, 0, 20, controller.signal);
                        fetchedThreads = data.content || [];
                        localHasMore = !data.last;
                    } catch (e) {
                         if (e.name === 'CanceledError' || e.name === 'AbortError' || e.message === 'canceled') return;
                         console.error("Auto fetch older failed:", e);
                    }
                }

                if (!controller.signal.aborted) {
                    setThreads(fetchedThreads);
                    setHasMoreLocal(localHasMore);
                }
            } catch (error) {
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
    }, [folder, activeTab]);

	// Fetch thread detail when selectedThreadId changes
	useEffect(() => {
		if (!selectedThreadId) {
			setSelectedThreadData(null);
			return;
		}

		const controller = new AbortController();

		const fetchDetail = async () => {
			setIsLoadingDetail(true);
			try {
				const data = await mailService.fetchThreadDetail(selectedThreadId, controller.signal);
				setSelectedThreadData(data);
			} catch (error) {
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

	const handleLoadMore = async () => {
		if (isLoadingMore) return;
		setIsLoadingMore(true);
        const signal = fetchControllerRef.current?.signal;

		try {
			const labelIds = getLabelIds();

			if (hasMoreLocal) {
				const nextDbPage = Math.floor(threads.length / 20);
				const data = await mailService.fetchEmails(labelIds, nextDbPage, 20, signal);
				
                if (signal?.aborted) return;
				setThreads(prev => {
					const existingIds = new Set(prev.map(t => t.id));
					const newItems = (data.content || []).filter(t => !existingIds.has(t.id));
					return [...prev, ...newItems];
				});
				setDbPage(nextDbPage);
				setHasMoreLocal(!data.last);
			} else if (hasMoreGoogle) {
				// Get the oldest timestamp in milliseconds from current list
				const oldestThread = threads.length > 0 ? threads[threads.length - 1] : null;
				const beforeTimestamp = oldestThread ? oldestThread.lastMessageTimestamp : null;

				// Fallback to fetch older from google
				const googleData = await mailService.fetchOlderFromGoogle(labelIds, googlePageToken, beforeTimestamp, signal);
				
                if (signal?.aborted) return;
				setGooglePageToken(googleData.nextPageToken);
				if (!googleData.nextPageToken) {
					setHasMoreGoogle(false);
				}

				// Then try fetching again from local DB using correct page index
				const currentDbPage = Math.floor(threads.length / 20);
				const data = await mailService.fetchEmails(labelIds, currentDbPage, 20, signal);
				
                if (signal?.aborted) return;
				setThreads(prev => {
					const existingIds = new Set(prev.map(t => t.id));
					const newItems = (data.content || []).filter(t => !existingIds.has(t.id));
					return [...prev, ...newItems];
				});
				
				setDbPage(currentDbPage);
				setHasMoreLocal(!data.last);
			}
		} catch (error) {
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

	const hasMore = hasMoreLocal || hasMoreGoogle;

	return (
		<>
			<InboxList
				folder={folder}
				threads={displayThreads}
				isLoading={isLoadingThreads}
				selectedThreadId={selectedThreadId}
				onSelectThread={setSelectedThreadId}
				activeTab={activeTab}
				onTabChange={setActiveTab}
				showUnreadOnly={showUnreadOnly}
				onToggleUnread={() => setShowUnreadOnly(!showUnreadOnly)}
				onLoadMore={handleLoadMore}
				hasMore={hasMore}
				isLoadingMore={isLoadingMore}
			/>
			<EmailReader
				folder={folder}
				selectedThread={selectedThreadData}
				isLoading={isLoadingDetail}
			/>
		</>
	);
}
