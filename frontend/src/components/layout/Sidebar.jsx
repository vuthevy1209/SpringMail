import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Inbox, Send, FileText, Trash2, Settings, Mail } from 'lucide-react';

const iconMap = {
    Inbox: Inbox,
    Send: Send,
    FileText: FileText,
    Trash2: Trash2,
};

export const folders = [
    { id: 'inbox',  name: 'Inbox',  icon: 'Inbox',    count: 12 },
    { id: 'sent',   name: 'Sent',   icon: 'Send',     count: 0  },
    { id: 'drafts', name: 'Drafts', icon: 'FileText', count: 3  },
    { id: 'trash',  name: 'Trash',  icon: 'Trash2',   count: 0  },
];

export default function Sidebar() {
    const location = useLocation();
    const navigate = useNavigate();

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

            {/* Settings */}
            <div className="mt-auto">
                <button className="flex items-center gap-3 px-3 py-2.5 w-full text-muted-steel font-medium rounded-lg hover:bg-white/60 transition-colors">
                    <Settings size={18} />
                    <span className="text-sm">Settings</span>
                </button>
            </div>
        </div>
    );
}

