import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import GlobalLoading from '../layout/GlobalLoading';

/**
 * Navigation guard that intercepts requests to private routes.
 * While checking session: Shows GlobalLoading.
 * If not logged in: Redirects to /login.
 * If logged in: Renders the nested routes via Outlet.
 */
export default function ProtectedRoute() {
	const { authStatus } = useAuth();

	if (authStatus === 'loading') {
		return <GlobalLoading />;
	}

	if (authStatus === 'unauthenticated') {
		return <Navigate to="/login" replace />;
	}

	return <Outlet />;
}
