import React, { createContext, useContext, useState, useEffect } from 'react';
import authService from '../services/authService';

export interface User {
    id?: string;
    email?: string;
    name?: string;
    picture?: string;
    syncStatus?: 'INITIAL_SYNC_IN_PROGRESS' | 'SYNCED' | 'ERROR' | string;
    initialSyncProgress?: number;
}

export interface AuthContextType {
    user: User | null;
    authStatus: 'loading' | 'authenticated' | 'unauthenticated' | string;
    checkAuth: () => Promise<void>;
    logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType>({
        user: null,
        authStatus: 'loading',
        checkAuth: () => Promise.resolve(),
        logout: () => Promise.resolve(),
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
        const [user, setUser] = useState<User | null>(null);
        const [authStatus, setAuthStatus] = useState<string>('loading'); // 'loading' | 'authenticated' | 'unauthenticated'

        const checkAuth = async () => {
                const startTime = Date.now();
                const MIN_DELAY = 1000;

		try {
			const userData = await authService.checkAuthStatus();
			
			// Calculate how much longer we need to wait
			const elapsed = Date.now() - startTime;
			if (elapsed < MIN_DELAY) {
				await new Promise(resolve => setTimeout(resolve, MIN_DELAY - elapsed));
			}

			setUser(userData);
			setAuthStatus('authenticated');
		} catch (error) {
			console.error('Auth verification failed:', error);
			
			// Even on error, wait a bit so the transition isn't jarring
			const elapsed = Date.now() - startTime;
			if (elapsed < MIN_DELAY) {
				await new Promise(resolve => setTimeout(resolve, MIN_DELAY - elapsed));
			}

			setUser(null);
			setAuthStatus('unauthenticated');
		}
	};

	useEffect(() => {
		checkAuth();
	}, []);

	const logout = async () => {
		try {
			await authService.logout();
		} finally {
			setUser(null);
			setAuthStatus('unauthenticated');
		}
	};

	return (
		<AuthContext.Provider value={{ user, authStatus, checkAuth, logout }}>
			{children}
		</AuthContext.Provider>
	);
}

export function useAuth() {
	return useContext(AuthContext);
}
