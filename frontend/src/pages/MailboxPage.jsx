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

	// Fetch emails whenever folder changes or inbox tab changes
	useEffect(() => {
		const fetchLatestEmails = async () => {
			setThreads([]);
			setSelectedThreadId(null);
			setSelectedThreadData(null);
			setIsLoadingThreads(true);

			try {
				const data = await mailService.fetchEmails(folder, activeTab);
				setThreads(data);
			} finally {
				setIsLoadingThreads(false);
			}
		};

		fetchLatestEmails();
	}, [folder, activeTab]);

	// Fetch thread detail when selectedThreadId changes
	useEffect(() => {
		if (!selectedThreadId) {
			setSelectedThreadData(null);
			return;
		}

		const fetchDetail = async () => {
			setIsLoadingDetail(true);
			try {
				const data = await mailService.fetchThreadDetail(selectedThreadId);
				setSelectedThreadData(data);
			} catch (error) {
				console.error("Failed to fetch thread detail:", error);
			} finally {
				setIsLoadingDetail(false);
			}
		};

		fetchDetail();
	}, [selectedThreadId]);

	return (
		<>
			<InboxList
				folder={folder}
				threads={threads}
				isLoading={isLoadingThreads}
				selectedThreadId={selectedThreadId}
				onSelectThread={setSelectedThreadId}
				activeTab={activeTab}
				onTabChange={setActiveTab}
			/>
			<EmailReader
				folder={folder}
				selectedThread={selectedThreadData}
				isLoading={isLoadingDetail}
			/>
		</>
	);
}
