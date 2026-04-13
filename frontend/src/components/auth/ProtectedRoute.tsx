import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import GlobalLoading from '../layout/GlobalLoading';

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
