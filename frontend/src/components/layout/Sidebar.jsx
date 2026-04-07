import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Inbox, Send, FileText, Trash2, Settings, Mail, LogOut, X } from 'lucide-react';
import authService from '../../service/authService';

const iconMap = {
    Inbox: Inbox,
    Send: Send,
    FileText: FileText,
    Trash2: Trash2,
};

export const folders = [
    { id: 'inbox', name: 'Inbox', icon: 'Inbox', count: 12 },
    { id: 'sent', name: 'Sent', icon: 'Send', count: 0 },
    { id: 'drafts', name: 'Drafts', icon: 'FileText', count: 3 },
    { id: 'trash', name: 'Trash', icon: 'Trash2', count: 0 },
];

export default function Sidebar() {
    const location = useLocation();
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [isSettingsOpen, setIsSettingsOpen] = useState(false);

    useEffect(() => {
        const loadUser = () => {
            const storedUser = localStorage.getItem('user');
            if (storedUser) {
                try {
                    setUser(JSON.parse(storedUser));
                } catch (e) {
                    console.error("Failed to parse user from localStorage", e);
                }
            } else {
                setUser(null);
            }
        };

        loadUser();
        window.addEventListener('user-profile-updated', loadUser);

        return () => {
            window.removeEventListener('user-profile-updated', loadUser);
        };
    }, []);

    const handleLogout = async () => {
        try {
            await authService.logout();
        } catch (error) {
            console.error('Logout failed', error);
        } finally {
            navigate('/login');
        }
    };

    // Active folder = the first path segment, e.g. "/inbox" → "inbox"
    const activeFolder = location.pathname.replace('/', '').split('/')[0] || 'inbox';

    return (
        <div className="w-[240px] bg-canvas-gray border-r border-whisper/50 flex flex-col h-screen px-4 py-6">

            {/* Brand */}
            <div className="flex items-center gap-3 mb-6 px-2">
                <div className="bg-emerald-accent text-white p-1.5 rounded-lg flex items-center">
                    <Mail size={18} />
                </div>
                <span className="font-bold tracking-tight text-lg">SpringMail</span>
            </div>

            {/* Folders */}
            <div className="flex flex-col gap-1 flex-1">
                {folders.map(folder => {
                    const Icon = iconMap[folder.icon];
                    const isActive = activeFolder === folder.id;

                    return (
                        <button
                            key={folder.id}
                            onClick={() => navigate(`/${folder.id}`)}
                            className={`flex items-center justify-between px-3 py-2.5 rounded-lg border transition-all ${isActive
                                ? 'bg-pure-surface text-charcoal-ink font-semibold shadow-sm border-whisper/50'
                                : 'bg-transparent text-muted-steel font-medium border-transparent hover:bg-white/60'
                                }`}
                        >
                            <div className="flex items-center gap-3">
                                <Icon
                                    size={18}
                                    className={isActive ? 'text-emerald-accent' : ''}
                                />
                                <span className="text-sm">{folder.name}</span>
                            </div>
                            {folder.count > 0 && (
                                <span
                                    className={`text-xs px-2 py-0.5 rounded-xl font-semibold ${isActive
                                        ? 'bg-emerald-accent text-white'
                                        : 'bg-transparent text-muted-steel'
                                        }`}
                                >
                                    {folder.count}
                                </span>
                            )}
                        </button>
                    );
                })}
            </div>

            {/* User Profile */}
            {user && (
                <div className="mt-auto mb-2 pt-4 border-t border-whisper/50 flex items-center gap-3 px-2">
                    <img
                        src={user.avatar}
                        alt="User avatar"
                        className="w-9 h-9 rounded-full object-cover border border-whisper shrink-0"
                        referrerPolicy="no-referrer"
                    />
                    <div className="flex flex-col overflow-hidden w-full">
                        <span className="text-[13px] font-semibold text-charcoal-ink truncate">{user.name || user.given_name}</span>
                        <span className="text-xs text-muted-steel truncate">{user.email}</span>
                    </div>

                    {/* Settings */}
                    <div>
                        <button 
                            onClick={() => setIsSettingsOpen(true)}
                            className="flex items-center gap-3 p-1.5 text-muted-steel rounded-lg hover:bg-white/80 transition-colors"
                        >
                            <Settings size={18} />
                        </button>
                    </div>
                </div>
            )}

            {/* Settings Modal Overlay */}
            {isSettingsOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/20 backdrop-blur-[2px]">
                    <div className="bg-pure-surface border border-whisper w-[320px] rounded-2xl shadow-xl flex flex-col overflow-hidden">
                        
                        {/* Header */}
                        <div className="flex justify-between items-center p-4 border-b border-whisper/50 bg-canvas-gray/50">
                            <h3 className="font-semibold text-charcoal-ink text-[15px]">Profile & Settings</h3>
                            <button 
                                onClick={() => setIsSettingsOpen(false)}
                                className="text-muted-steel hover:text-charcoal-ink transition-colors p-1 rounded-md hover:bg-white/80 shrink-0"
                            >
                                <X size={18} />
                            </button>
                        </div>

                        {/* Body - User Info */}
                        {user && (
                            <div className="p-6 flex flex-col items-center text-center gap-3">
                                <img 
                                    src={user.avatar} 
                                    alt="Avatar" 
                                    className="w-16 h-16 rounded-full border border-whisper object-cover shadow-sm"
                                    referrerPolicy="no-referrer"
                                />
                                <div>
                                    <h4 className="font-bold text-charcoal-ink">{user.name || user.given_name}</h4>
                                    <p className="text-sm text-muted-steel">{user.email}</p>
                                </div>
                            </div>
                        )}

                        {/* Actions */}
                        <div className="p-4 pt-2 pb-5">
                            <button 
                                onClick={handleLogout}
                                className="w-full flex items-center justify-center gap-2 bg-red-50 text-red-600 font-semibold py-2.5 rounded-lg border border-red-100 hover:bg-red-100 active:bg-red-200 transition-colors"
                            >
                                <LogOut size={16} />
                                Sign Out
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

