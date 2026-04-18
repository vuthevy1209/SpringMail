import React from 'react';
import { Thread } from '../../types/mail';

interface ThreadItemProps {
    thread: Thread;
    isSelected: boolean;
    onClick: () => void;
}

export default function ThreadItem({ thread, isSelected, onClick }: ThreadItemProps) {
    // Use the first sender from the senderNames list
    const senderName = (thread.senderNames && thread.senderNames.length > 0) 
        ? thread.senderNames[0] 
        : 'Unknown';
    const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(senderName)}&background=random&color=fff&rounded=true&bold=true`;

    return (
        <div 
            onClick={onClick}
            className={`px-5 py-4 border-b border-whisper/50 cursor-pointer transition-all flex gap-3 ${
                isSelected
                    ? 'bg-canvas-gray opacity-100'
                    : thread.unread
                    ? 'bg-pure-surface hover:bg-[#f9fafa] opacity-100'
                    : 'bg-transparent hover:bg-canvas-gray/60 opacity-60 grayscale-[10%]'
            }`}
        >
            {/* Avatar */}
            <div className="relative w-10 h-10 shrink-0">
                <img 
                    src={avatarUrl} 
                    alt={senderName} 
                    className="w-full h-full rounded-full" 
                />
            </div>

            {/* Content */}
            <div className="flex-1 min-w-0">
                <div className="flex justify-between items-baseline mb-1">
                    <div className="flex items-center gap-2 truncate">
                        <span className={`text-charcoal-ink text-sm truncate ${thread.unread ? 'font-bold' : 'font-medium'}`}>
                            {senderName}
                        </span>
                        {thread.unread && (
                            <span className="shrink-0 bg-spring-green/10 text-spring-green text-[10px] font-bold px-1.5 py-0.5 rounded uppercase tracking-wider">
                                New
                            </span>
                        )}
                    </div>
                    <div className="flex gap-1.5 items-center shrink-0 ml-2">
                        {thread.messageCount > 1 && (
                            <span className="bg-gray-200 text-gray-600 text-[10px] font-bold px-1.5 py-0.5 rounded">
                                {thread.messageCount}
                            </span>
                        )}
                        <span className={`text-xs whitespace-nowrap ${
                            thread.unread
                                ? 'text-charcoal-ink font-semibold'
                                : 'text-muted-steel font-normal'
                        }`}>
                            {thread.internalDate ? new Date(thread.internalDate).toLocaleDateString() : ''}
                        </span>
                    </div>
                </div>
                <div className={`text-sm text-charcoal-ink mb-1 truncate ${thread.unread ? 'font-semibold' : 'font-medium'}`}>
                    {thread.subject || "(No subject)"}
                </div>
                <div className="text-[13px] text-muted-steel line-clamp-2 leading-[1.4]">
                    {thread.snippet}
                </div>
            </div>
        </div>
    );
}
