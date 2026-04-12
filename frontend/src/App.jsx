import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import MailboxPage from './pages/MailboxPage';
import MainLayout from './components/layout/MainLayout';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/auth/ProtectedRoute';

export default function App() {
	return (
		<AuthProvider>
			<Routes>
				{/* Public Routes */}
				<Route path="/login" element={<LoginPage />} />

				{/* Protected Routes */}
				<Route element={<ProtectedRoute />}>
					{/* Root redirect */}
					<Route path="/" element={<Navigate to="/inbox" replace />} />

					{/* App Routes (with layout) */}
					<Route element={<MainLayout />}>
						<Route path="/inbox"      element={<MailboxPage folder="inbox"     />} />
						<Route path="/all"        element={<MailboxPage folder="all"       />} />
						<Route path="/starred"    element={<MailboxPage folder="starred"   />} />
						<Route path="/important"  element={<MailboxPage folder="important" />} />
						<Route path="/sent"       element={<MailboxPage folder="sent"      />} />
						<Route path="/drafts"     element={<MailboxPage folder="drafts"    />} />
						<Route path="/spam"       element={<MailboxPage folder="spam"      />} />
						<Route path="/trash"      element={<MailboxPage folder="trash"     />} />
					</Route>
				</Route>
			</Routes>
		</AuthProvider>
	);
}
