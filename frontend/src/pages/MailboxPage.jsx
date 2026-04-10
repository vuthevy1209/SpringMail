import React, { useState, useEffect } from 'react';
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

	// For inbox only — which category tab is active
	const [activeTab, setActiveTab] = useState('primary');

	// Filter unread only
	const [showUnreadOnly, setShowUnreadOnly] = useState(false);



	// Fetch emails whenever folder changes or inbox tab changes
	useEffect(() => {
		const controller = new AbortController();

		const fetchLatestEmails = async () => {
			setThreads([]);
			setSelectedThreadId(null);
			setSelectedThreadData(null);
			setIsLoadingThreads(true);
			setShowUnreadOnly(false); // Reset filter when switching folders

			try {
				const data = await mailService.fetchEmails(folder, activeTab, controller.signal);
				setThreads(data);
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

	const displayThreads = showUnreadOnly 
		? threads.filter(t => !t.isRead) 
		: threads;

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
			/>
			<EmailReader
				folder={folder}
				selectedThread={selectedThreadData}
				isLoading={isLoadingDetail}
			/>
		</>
	);
}
