import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import authService from '../service/authService';
import mailService from '../service/mailService';
import InboxList from '../components/InboxList';
import EmailReader from '../components/EmailReader';

/**
 * Generic mailbox page shared by all folder routes:
 *   /inbox   → folder="inbox"
 *   /sent    → folder="sent"
 *   /drafts  → folder="drafts"
 *   /trash   → folder="trash"
 */
export default function MailboxPage({ folder }) {
	const [isAuthenticated, setIsAuthenticated] = useState(null);
	const [threads, setThreads] = useState([]);
	const [isLoadingThreads, setIsLoadingThreads] = useState(false);
	const [selectedThreadId, setSelectedThreadId] = useState(null);

	// For inbox only — which category tab is active
	const [activeTab, setActiveTab] = useState('primary');

	useEffect(() => {
		const checkAuth = async () => {
			try {
				await authService.checkAuthStatus();
				setIsAuthenticated(true);
			} catch {
				setIsAuthenticated(false);
			}
		};
		checkAuth();
	}, []);

	// Fetch emails whenever auth resolves, folder changes, or inbox tab changes
	useEffect(() => {
		if (!isAuthenticated) return;

		const fetchLatestEmails = async () => {
			setThreads([]);
			setSelectedThreadId(null);
			setIsLoadingThreads(true);

			try {
				const data = await mailService.fetchEmails(folder, activeTab);
				setThreads(data);
			} finally {
				setIsLoadingThreads(false);
			}
		};

		fetchLatestEmails();
	}, [isAuthenticated, folder, activeTab]);

	if (isAuthenticated === null) {
		return (
			<div className="flex flex-1 justify-center items-center h-screen bg-canvas-gray text-muted-steel">
				Loading session details...
			</div>
		);
	}

	if (isAuthenticated === false) {
		return <Navigate to="/login" replace />;
	}

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
				threads={threads}
				selectedThreadId={selectedThreadId}
			/>
		</>
	);
}
