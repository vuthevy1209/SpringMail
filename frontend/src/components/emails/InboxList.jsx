import { Search, Inbox, Users, Tag, Info } from 'lucide-react';
import ThreadItem from './ThreadItem';
import InboxListSkeleton from './InboxListSkeleton';
import { LAYOUT } from '../../constants/layout';

export default function InboxList({ 
    folder = 'inbox', 
    selectedThreadId, 
    onSelectThread, 
    threads = [], 
    isLoading = false, 
    activeTab = 'primary', 
    onTabChange 
}) {
    const folderTitle = {
        inbox:  'Inbox',
        sent:   'Sent',
        drafts: 'Drafts',
        trash:  'Trash',
    }[folder] ?? 'Inbox';

    const tabs = [
        { id: 'primary',    label: 'Primary',    icon: Inbox },
        { id: 'promotions', label: 'Promotions', icon: Tag   },
        { id: 'social',     label: 'Social',     icon: Users },
        { id: 'updates',    label: 'Updates',    icon: Info  },
    ];

    return (
        <div className={`${LAYOUT.INBOX_LIST_WIDTH} bg-pure-surface ${LAYOUT.COLUMN_BORDER} flex flex-col h-screen`}>

            {/* Header & Search */}
            <div className="px-5 pt-6 pb-4 border-b border-whisper/50">
                <h2 className="text-xl text-charcoal-ink mb-4">{folderTitle}</h2>
                <div className="flex items-center bg-canvas-gray px-3 py-2 rounded-lg gap-2">
                    <Search size={16} className="text-muted-steel shrink-0" />
                    <input
                        type="text"
                        placeholder="Search mail or ask AI..."
                        className="border-none bg-transparent outline-none w-full text-sm font-sans text-charcoal-ink placeholder:text-muted-steel"
                    />
                </div>
            </div>

            {/* Tabs — only shown for inbox */}
            {folder === 'inbox' && (
                <div className="flex border-b border-whisper/50 bg-canvas-gray">
                    {tabs.map(tab => {
                        const Icon = tab.icon;
                        const isActive = activeTab === tab.id;
                        return (
                            <button
                                key={tab.id}
                                onClick={() => onTabChange && onTabChange(tab.id)}
                                className={`flex-1 flex flex-col items-center py-3 bg-transparent cursor-pointer transition-all gap-1 border-b-2 ${
                                    isActive
                                        ? 'text-emerald-accent border-emerald-accent font-semibold'
                                        : 'text-muted-steel border-transparent font-medium'
                                }`}
                            >
                                <Icon size={18} className="shrink-0" />
                                <span className="text-[11px]">{tab.label}</span>
                            </button>
                        );
                    })}
                </div>
            )}

            {/* Thread List */}
            <div className="flex-1 overflow-y-auto">
                {isLoading ? (
                    <InboxListSkeleton />
                ) : threads.length === 0 ? (
                    <div className="text-center py-10 px-5 text-muted-steel text-sm">
                        No threads found.
                    </div>
                ) : (
                    threads.map((thread) => (
                        <ThreadItem 
                            key={thread.id}
                            thread={thread}
                            isSelected={thread.id === selectedThreadId}
                            onClick={() => onSelectThread(thread.id)}
                        />
                    ))
                )}
            </div>
        </div>
    );
}
