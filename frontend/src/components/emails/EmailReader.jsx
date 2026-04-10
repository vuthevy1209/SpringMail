import { Sparkles, ListChecks, Paperclip, FileText } from 'lucide-react';
import EmailBody from './EmailBody';
import EmailReaderSkeleton from './EmailReaderSkeleton';
import { LAYOUT } from '../../constants/layout';

export default function EmailReader({ selectedThread, isLoading }) {
    if (isLoading) {
        return <EmailReaderSkeleton />;
    }

    if (!selectedThread) {
        return (
            <div className="flex-1 flex items-center justify-center bg-canvas-gray">
                <div className="text-center text-muted-steel">
                    <Sparkles size={32} className="mx-auto mb-4 opacity-50" />
                    <p>Select a thread to read</p>
                </div>
            </div>
        );
    }

    const thread = selectedThread;

    return (
        <div className={LAYOUT.MAIN_CONTENT}>

            {/* LLM Actions Toolbar */}
            <div className="px-6 py-4 border-b border-whisper/50 flex gap-3 bg-pure-surface">
                <button className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors">
                    <Sparkles size={16} className="text-spring-green" />
                    Summarize Thread
                </button>
                <button className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors">
                    <ListChecks size={16} />
                    Extract Action Items
                </button>
                <div className="flex-1" />
            </div>

            {/* Reading Pane */}
            <div className="flex-1 px-12 py-8 overflow-y-auto">
                <div className="max-w-[800px] mx-auto">
                    <h1 className="text-2xl text-charcoal-ink mb-8 font-semibold">
                        {thread.subject}
                    </h1>

                    {/* Stack of Messages in the Thread */}
                    <div className="space-y-6">
                        {thread.messages && thread.messages.map((email) => (
                            <div key={email.id} className="bg-pure-surface rounded-xl p-6 border border-whisper shadow-sm">
                                {/* Sender Info */}
                                <div className="flex justify-between items-center mb-6">
                                    <div className="flex items-center gap-3">
                                        <div className="w-10 h-10 shrink-0">
                                            <img
                                                src={`https://ui-avatars.com/api/?name=${encodeURIComponent(email.senderName || 'Unknown')}&background=random&color=fff&rounded=true&bold=true`}
                                                alt={email.senderName}
                                                className="w-full h-full rounded-full"
                                            />
                                        </div>
                                        <div>
                                            <div className="font-semibold text-charcoal-ink">
                                                {email.senderName || '(Unknown)'}
                                            </div>
                                            <div className="text-[13px] text-muted-steel">
                                                {email.senderEmail ? `<${email.senderEmail}>` : ''}
                                            </div>
                                        </div>
                                    </div>
                                    <div className="text-muted-steel text-sm">
                                        {email.date ? new Date(email.date).toLocaleString() : ''}
                                    </div>
                                </div>

                                {/* Isolated Email Body */}
                                <div className="text-charcoal-ink text-[15px] leading-relaxed break-words">
                                    <EmailBody 
                                        content={email.content} 
                                        messageId={email.id}
                                        attachments={email.attachments}
                                    />
                                </div>

                                {/* Attachments */}
                                {email.attachments && email.attachments.filter(att => !att.contentId).length > 0 && (
                                    <div className="mt-6 pt-4 border-t border-whisper/50">
                                        <div className="font-semibold text-charcoal-ink mb-3 flex items-center gap-2 text-sm">
                                            <Paperclip size={14} />
                                            {email.attachments.filter(att => !att.contentId).length} Attachments
                                        </div>
                                        <div className="flex flex-wrap gap-2">
                                            {email.attachments
                                                .filter(att => !att.contentId)
                                                .map((att) => (
                                                <a
                                                    key={att.id}
                                                    href={`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/get-attachment?messageId=${email.id}&attachmentId=${att.id}&filename=${encodeURIComponent(att.filename)}&contentType=${encodeURIComponent(att.mimeType)}`}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    className="flex items-center gap-2 px-3 py-2 border border-whisper/50 rounded-lg bg-canvas-gray text-[12px] text-charcoal-ink shadow-sm hover:bg-whisper/30 transition-colors"
                                                >
                                                    <FileText size={14} className="text-spring-green" />
                                                    <span className="max-w-[200px] truncate" title={att.filename}>{att.filename}</span>
                                                </a>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}
