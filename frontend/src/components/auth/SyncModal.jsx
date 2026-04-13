import React, { useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Loader2, RefreshCw } from 'lucide-react';

export default function SyncModal() {
    const { user, checkAuth } = useAuth();
    
    useEffect(() => {
        if (user?.syncStatus === 'INITIAL_SYNC_IN_PROGRESS') {
            const interval = setInterval(() => {
                checkAuth();
            }, 3000); // Poll every 3 seconds
            
            return () => clearInterval(interval);
        }
    }, [user?.syncStatus, checkAuth]);

    if (!user || user.syncStatus !== 'INITIAL_SYNC_IN_PROGRESS') {
        return null;
    }

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-charcoal-ink/40 backdrop-blur-sm">
            <div className="bg-pure-surface rounded-2xl shadow-2xl w-[420px] p-8 flex flex-col items-center animate-in zoom-in-95 duration-300">
                <div className="relative mb-6">
                    <div className="absolute inset-0 bg-spring-green/20 rounded-full animate-ping"></div>
                    <div className="relative bg-spring-green/10 text-spring-green p-4 rounded-full">
                        <RefreshCw size={36} className="animate-spin text-spring-green" />
                    </div>
                </div>
                
                <h2 className="text-xl font-bold text-charcoal-ink mb-3 text-center">
                    Syncing Your Mailbox
                </h2>
                
                <p className="text-muted-steel text-center text-[15px] leading-relaxed mb-8">
                    We are performing the initial synchronization of your Gmail account. 
                    This might take a few moments depending on the size of your mailbox.
                </p>
                
                <div className="w-full bg-canvas-gray rounded-full h-2 mb-2 overflow-hidden border border-whisper">
                    <div 
                        className="bg-spring-green h-2 rounded-full transition-all duration-500 ease-out" 
                        style={{ width: `${user.initialSyncProgress || 5}%` }}
                    ></div>
                </div>
                <p className="text-xs text-muted-steel font-medium tracking-wide/50 w-full flex justify-between mt-2">
                    <span className="animate-pulse">FETCHING EMAILS...</span>
                    <span>{user.initialSyncProgress || 0}%</span>
                </p>
            </div>
        </div>
    );
}
