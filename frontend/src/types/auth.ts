export interface User {
    id: string;
    email: string;
    name: string;
    picture?: string;
    syncStatus?: 'INITIAL_SYNC_IN_PROGRESS' | 'SYNCED' | 'ERROR';
    initialSyncProgress?: number;
}
export interface AuthContextType {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (token: string) => Promise<void>;
    logout: () => void;
    checkAuth: () => Promise<void>;
}
