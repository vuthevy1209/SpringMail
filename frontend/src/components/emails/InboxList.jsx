import { Search, Inbox, Users, Tag, Info, Filter } from 'lucide-react';
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
    onTabChange,
    showUnreadOnly = false,
    onToggleUnread,
    onLoadMore,
    hasMore,
    isLoadingMore
}) {
    const folderTitle = {
        inbox:     'Inbox',
        all:       'All Mail',
        starred:   'Starred',
        important: 'Important',
        sent:      'Sent',
        drafts:    'Drafts',
        spam:      'Spam',
        trash:     'Trash',
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
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-xl font-bold text-charcoal-ink tracking-tight">{folderTitle}</h2>
                    <button 
                        onClick={onToggleUnread}
                        className={`
                            flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-semibold
                            transition-all duration-300 ease-out cursor-pointer border
                            ${showUnreadOnly 
                                ? 'bg-spring-green text-pure-surface border-spring-green shadow-md shadow-spring-green/20 scale-105' 
                                : 'bg-canvas-gray text-muted-steel border-whisper/50 hover:border-muted-steel/30 hover:bg-whisper/20'}
                        `}
                    >
                        <Filter size={14} strokeWidth={2.5} />
                        <span>Unread</span>
                    </button>
                </div>
                <div className="flex items-center bg-canvas-gray px-3 py-2.5 rounded-xl gap-3 border border-transparent transition-all duration-300 focus-within:bg-pure-surface focus-within:border-spring-green/30 focus-within:shadow-xl focus-within:shadow-spring-green/5">
                    <Search size={18} className="text-muted-steel shrink-0 transition-colors group-focus-within:text-spring-green" />
                    <input
                        type="text"
                        placeholder="Search mail or ask AI..."
                        className="bg-transparent border-none outline-none w-full text-sm font-medium text-charcoal-ink placeholder:text-muted-steel/50"
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
                                        ? 'text-spring-green border-spring-green font-semibold'
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
            <div className="flex-1 overflow-y-auto pb-6">
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

                {!isLoading && threads.length > 0 && hasMore && (
                    <>
                        {isLoadingMore ? (
                            <div className="mt-2">
                                <InboxListSkeleton count={8} />
                            </div>
                        ) : (
                            <div className="flex justify-center mt-6 mb-6">
                                <button 
                                    onClick={onLoadMore}
                                    className="px-6 py-2 rounded-full text-sm font-semibold bg-canvas-gray text-charcoal-ink border border-whisper/50 hover:bg-whisper/20 transition-all font-medium cursor-pointer"
                                >
                                    Show More
                                </button>
                            </div>
                        )}
                    </>
                )}
                {!isLoading && threads.length > 0 && !hasMore && (
                    <div className="text-center mt-6 mb-4 text-xs text-muted-steel">
                        No more emails.
                    </div>
                )}
            </div>
        </div>
    );
}
