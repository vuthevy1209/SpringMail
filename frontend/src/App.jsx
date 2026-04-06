import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import MailboxPage from './pages/MailboxPage';
import MainLayout from './components/layout/MainLayout';

export default function App() {
	return (
		<Routes>
			{/* Root redirect */}
			<Route path="/" element={<Navigate to="/inbox" replace />} />

			{/* Login Route (no layout) */}
			<Route path="/login" element={<LoginPage />} />

			{/* App Routes (with layout) */}
			<Route element={<MainLayout />}>
				<Route path="/inbox"  element={<MailboxPage folder="inbox"  />} />
				<Route path="/sent"   element={<MailboxPage folder="sent"   />} />
				<Route path="/drafts" element={<MailboxPage folder="drafts" />} />
				<Route path="/trash"  element={<MailboxPage folder="trash"  />} />
			</Route>
		</Routes>
	);
}
