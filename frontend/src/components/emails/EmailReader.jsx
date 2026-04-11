import { useState } from "react";
import {
    Sparkles,
    ListChecks,
    Paperclip,
    FileText,
    Archive,
    AlertTriangle,
    Trash2,
    Mail,
    FolderInput,
    Reply,
    Forward,
} from "lucide-react";
import mailService from "../../service/mailService";
import EmailBody from "./EmailBody";
import EmailReaderSkeleton from "./EmailReaderSkeleton";
import { LAYOUT } from "../../constants/layout";

export default function EmailReader({ selectedThread, isLoading }) {
    const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
    const [actionToConfirm, setActionToConfirm] = useState(null);

    const handleActionClick = (actionName) => {
        setActionToConfirm(actionName);
        setIsConfirmModalOpen(true);
    };

    const handleConfirmAction = () => {
        // TODO: Implement logic here based on actionToConfirm
        // console.log("Confirmed action:", actionToConfirm);
        setIsConfirmModalOpen(false);
        setActionToConfirm(null);
    };

    const handleCancelAction = () => {
        setIsConfirmModalOpen(false);
        setActionToConfirm(null);
    };

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
            {/* Actions Toolbar */}
            <div className="px-6 py-4 border-b border-whisper/50 flex items-center justify-between bg-pure-surface">
                {/* Left side: AI Actions */}
                <div className="flex gap-2">
                    <button className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors">
                        <Sparkles size={16} className="text-spring-green" />
                        Summarize
                    </button>
                </div>

                {/* Right side: Email Actions */}
                <div className="flex flex-wrap gap-2">
                    <button 
                        onClick={() => handleActionClick('Archive')}
                        className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors"
                    >
                        <Archive size={16} />
                        <span>Archive</span>
                    </button>
                    <button 
                        onClick={() => handleActionClick('Report spam')}
                        className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors"
                    >
                        <AlertTriangle size={16} />
                        <span>Report spam</span>
                    </button>
                    <button 
                        onClick={() => handleActionClick('Delete')}
                        className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors"
                    >
                        <Trash2 size={16} />
                        <span>Delete</span>
                    </button>
                    <button 
                        onClick={() => handleActionClick('Mark as read')}
                        className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors"
                    >
                        <Mail size={16} />
                        <span>Mark as read</span>
                    </button>
                    <button className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors">
                        <FolderInput size={16} />
                        <span>Move to</span>
                    </button>
                </div>
            </div>

            {/* Reading Pane */}
            <div className="flex-1 px-12 py-8 overflow-y-auto">
                <div className="max-w-[800px] mx-auto">
                    <h1 className="text-2xl text-charcoal-ink mb-8 font-semibold">
                        {thread.subject}
                    </h1>

                    {/* Stack of Messages in the Thread */}
                    <div className="space-y-6">
                        {thread.messages &&
                            thread.messages.map((email) => (
                                <div
                                    key={email.id}
                                    className="bg-pure-surface rounded-xl p-6 border border-whisper shadow-sm"
                                >
                                    {/* Sender Info */}
                                    <div className="flex justify-between items-center mb-6">
                                        <div className="flex items-center gap-3">
                                            <div className="w-10 h-10 shrink-0">
                                                <img
                                                    src={`https://ui-avatars.com/api/?name=${encodeURIComponent(email.fromName || "Unknown")}&background=random&color=fff&rounded=true&bold=true`}
                                                    alt={email.fromName}
                                                    className="w-full h-full rounded-full"
                                                />
                                            </div>
                                            <div>
                                                <div className="font-semibold text-charcoal-ink">
                                                    {email.fromName || "(Unknown)"}
                                                </div>
                                                <div className="text-[13px] text-muted-steel">
                                                    {email.fromEmail ? `<${email.fromEmail}>` : ""}
                                                </div>
                                            </div>
                                        </div>
                                        <div className="text-muted-steel text-sm">
                                            {email.internalDate
                                                ? new Date(email.internalDate).toLocaleString()
                                                : ""}
                                        </div>
                                    </div>

                                    {/* Isolated Email Body */}
                                    <div className="text-charcoal-ink text-[15px] leading-relaxed break-words">
                                        <EmailBody
                                            content={email.bodyHtml || email.bodyText}
                                            messageId={email.id}
                                            attachments={email.attachments}
                                        />
                                    </div>

                                    {/* Attachments */}
                                    {email.attachments &&
                                        email.attachments.filter((att) => att.id).length > 0 && (
                                            <div className="mt-6 pt-4 border-t border-whisper/50">
                                                <div className="font-semibold text-charcoal-ink mb-3 flex items-center gap-2 text-sm">
                                                    <Paperclip size={14} />
                                                    {
                                                        email.attachments.filter((att) => att.id).length
                                                    }{" "}
                                                    Attachments
                                                </div>
                                                <div className="flex flex-wrap gap-2">
                                                    {email.attachments
                                                        .filter((att) => att.id)
                                                        .map((att) => (
                                                            <button
                                                                key={att.id}
                                                                onClick={() =>
                                                                    mailService.downloadAttachment(
                                                                        email.id,
                                                                        att.id,
                                                                        att.filename,
                                                                        att.mimeType,
                                                                    )
                                                                }
                                                                className="flex items-center gap-2 px-3 py-2 border border-whisper/50 rounded-lg bg-canvas-gray text-[12px] text-charcoal-ink shadow-sm hover:bg-whisper/30 transition-colors"
                                                            >
                                                                <FileText
                                                                    size={14}
                                                                    className="text-spring-green"
                                                                />
                                                                <span
                                                                    className="max-w-[200px] truncate"
                                                                    title={att.filename}
                                                                >
                                                                    {att.filename}
                                                                </span>
                                                            </button>
                                                        ))}
                                                </div>
                                            </div>
                                        )}
                                </div>
                            ))}
                    </div>

                    {/* Reply / Forward Actions */}
                    <div className="mt-8 flex gap-3 pb-8">
                        <button className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-6 py-2.5 rounded-full bg-pure-surface font-medium hover:bg-canvas-gray transition-colors shadow-sm">
                            <Reply size={18} className="text-muted-steel" />
                            Reply
                        </button>
                        <button className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-6 py-2.5 rounded-full bg-pure-surface font-medium hover:bg-canvas-gray transition-colors shadow-sm">
                            <Forward size={18} className="text-muted-steel" />
                            Forward
                        </button>
                    </div>
                </div>
            </div>

            {/* Confirmation Modal */}
            {isConfirmModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-charcoal-ink/50 backdrop-blur-sm">
                    <div className="bg-pure-surface rounded-xl shadow-lg w-full max-w-md overflow-hidden border border-whisper">
                        <div className="p-6">
                            <h3 className="text-lg font-semibold text-charcoal-ink mb-2">
                                Confirm Action
                            </h3>
                            <p className="text-muted-steel text-sm mb-6 leading-relaxed">
                                Are you sure you want to <strong className="text-charcoal-ink">{actionToConfirm?.toLowerCase()}</strong> the thread <strong className="text-charcoal-ink">"{thread.subject}"</strong>? This action might be irreversible.
                            </p>
                            <div className="flex justify-end gap-3">
                                <button
                                    onClick={handleCancelAction}
                                    className="px-4 py-2 rounded-lg font-medium text-charcoal-ink bg-canvas-gray hover:bg-whisper/50 transition-colors"
                                >
                                    Cancel
                                </button>
                                <button
                                    onClick={handleConfirmAction}
                                    className={`px-4 py-2 rounded-lg font-medium text-pure-surface bg-green-600 hover:bg-green-700 transition-colors`}
                                >
                                    Confirm
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
