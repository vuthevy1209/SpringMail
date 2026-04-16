import { Search, Inbox, Users, Tag, Info, Filter } from 'lucide-react';
import { useState, useEffect, useRef } from 'react';
import ThreadItem from './ThreadItem';
import InboxListSkeleton from './InboxListSkeleton';
import { LAYOUT } from '../../constants/layout';
import { Thread } from '../../types/mail';
import mailService from '../../services/mailService';

interface InboxListProps {
    folder?: string;
    selectedThreadId?: string | null;
    onSelectThread?: (id: string) => void;
    threads?: Thread[];
    searchQuery?: string;
    onSearchSubmit?: (query: string) => void;
    isLoading?: boolean;
    activeTab?: string;
    onTabChange?: (tab: string) => void;
    showUnreadOnly?: boolean;
    onToggleUnread?: () => void;
    onLoadMore?: () => void;
    hasMore?: boolean;
    isLoadingMore?: boolean;
}

export default function InboxList({ 
    folder = 'inbox', 
    selectedThreadId, 
    onSelectThread, 
    threads = [], 
    searchQuery = '',
    onSearchSubmit,
    isLoading = false, 
    activeTab = 'primary', 
    onTabChange,
    showUnreadOnly = false,
    onToggleUnread,
    onLoadMore,
    hasMore,
    isLoadingMore
}: InboxListProps) {
    const folderTitle: any = {
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

    const [inputValue, setInputValue] = useState(searchQuery);
    const [suggestions, setSuggestions] = useState<string[]>([]);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const containerRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        setInputValue(searchQuery);
    }, [searchQuery]);

    useEffect(() => {
        const fetchSuggestions = async () => {
            if (inputValue.trim().length > 1) {
                try {
                    const results = await mailService.suggestSubjects(inputValue);
                    setSuggestions(results);
                } catch (error) {
                    console.error("Error fetching suggestions:", error);
                }
            } else {
                setSuggestions([]);
            }
        };

        const timeoutId = setTimeout(fetchSuggestions, 300);
        return () => clearTimeout(timeoutId);
    }, [inputValue]);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                setShowSuggestions(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

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
                <div className="relative" ref={containerRef}>
                    <div className="flex items-center bg-canvas-gray px-3 py-2.5 rounded-xl gap-3 border border-transparent transition-all duration-300 focus-within:bg-pure-surface focus-within:border-spring-green/30 focus-within:shadow-xl focus-within:shadow-spring-green/5">
                        <button onClick={() => onSearchSubmit && onSearchSubmit(inputValue)} className="bg-transparent border-none p-0 cursor-pointer flex items-center justify-center outline-none">
                            <Search size={18} className="text-muted-steel shrink-0 transition-colors hover:text-spring-green group-focus-within:text-spring-green" />
                        </button>
                        <input
                            type="text"
                            value={inputValue}
                            onChange={(e) => {
                                setInputValue(e.target.value);
                                setShowSuggestions(true);
                            }}
                            onFocus={() => setShowSuggestions(true)}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter') {
                                    setShowSuggestions(false);
                                    if (onSearchSubmit) onSearchSubmit(inputValue);
                                }
                            }}
                            placeholder="Search mail or ask AI..."
                            className="bg-transparent border-none outline-none w-full text-sm font-medium text-charcoal-ink placeholder:text-muted-steel/50"
                        />
                    </div>
                    {showSuggestions && suggestions.length > 0 && (
                        <ul className="absolute z-10 w-full bg-pure-surface border border-whisper/50 rounded-xl shadow-lg mt-2 max-h-60 overflow-y-auto left-0 !p-2 !m-0 !mt-1 list-none box-border">
                            {suggestions.map((suggestion, index) => (
                                <li 
                                    key={index} 
                                    className="p-3 hover:bg-canvas-gray cursor-pointer text-sm text-charcoal-ink rounded-lg transition-colors truncate"
                                    onClick={() => {
                                        setInputValue(suggestion);
                                        setShowSuggestions(false);
                                        if (onSearchSubmit) onSearchSubmit(suggestion);
                                    }}
                                >
                                    <Search size={14} className="inline-block mr-2 text-muted-steel/70" />
                                    {suggestion}
                                </li>
                            ))}
                        </ul>
                    )}
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
                                onClick={() => {
                                    if (onSearchSubmit) {
                                        onSearchSubmit('');
                                    }
                                    if (onTabChange) {
                                        onTabChange(tab.id);
                                    }
                                }}
                                className={`flex-1 flex flex-col items-center py-3 bg-transparent cursor-pointer transition-all gap-1 border-b-2 ${
                                    (!searchQuery.trim() && isActive)
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
