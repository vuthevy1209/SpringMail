import React from 'react';
import { Navigate } from 'react-router-dom';
import api from '../service/api';
import InboxList from '../components/InboxList';
import ReaderComposer from '../components/ReaderComposer';
import { useState, useEffect } from 'react';

export default function InboxPage() {
	const [isAuthenticated, setIsAuthenticated] = useState(null);
	const [emails, setEmails] = useState([]);
	const [isLoadingEmails, setIsLoadingEmails] = useState(false);
	const [selectedEmailId, setSelectedEmailId] = useState(null);
	const [activeTab, setActiveTab] = useState('primary');

	useEffect(() => {
		const checkAuth = async () => {
			try {
				await api.get('/auth/me');
				setIsAuthenticated(true);
			} catch (err) {
				setIsAuthenticated(false);
			}
		};
		checkAuth();
	}, []);

	useEffect(() => {
		const fetchEmails = async () => {
			if (isAuthenticated) {
				setIsLoadingEmails(true);
				try {
					const emailRes = await api.get(`/get-emails?category=${activeTab}`);
					setEmails(emailRes.data);
				} catch (error) {
					console.error("Failed to fetch emails:", error);
				} finally {
					setIsLoadingEmails(false);
				}
			}
		};
		fetchEmails();
	}, [isAuthenticated, activeTab]);

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
				emails={emails}
				isLoading={isLoadingEmails}
				selectedEmailId={selectedEmailId}
				onSelectEmail={setSelectedEmailId}
				activeTab={activeTab}
				onTabChange={setActiveTab}
			/>
			<ReaderComposer
				emails={emails}
				selectedEmailId={selectedEmailId}
			/>
		</>
	);
}
