import { useState, useEffect } from "react";
import ReactMarkdown from "react-markdown";
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
    X,
    Inbox,
    Star,
    Bookmark,
    RefreshCw,
    Wand2,
} from "lucide-react";
import mailService from "../../service/mailService";
import EmailBody from "./EmailBody";
import EmailReaderSkeleton from "./EmailReaderSkeleton";
import { LAYOUT } from "../../constants/layout";

export default function EmailReader({ folder, selectedThread, isLoading, onThreadUpdated, onThreadDeleted }) {
    const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
    const [actionToConfirm, setActionToConfirm] = useState(null);
    const [summary, setSummary] = useState(null);
    const [isSummarizing, setIsSummarizing] = useState(false);
    const [isMoveToDropdownOpen, setIsMoveToDropdownOpen] = useState(false);
    const [isReplying, setIsReplying] = useState(false);
    const [replyContent, setReplyContent] = useState("");

    useEffect(() => {
        setSummary(null);
        setIsSummarizing(false);
        setIsReplying(false);
        setReplyContent("");

        if (selectedThread) {
            // Mark as read when thread is selected
            const hasUnread = selectedThread.messages?.some(msg => msg.labelIds?.includes("UNREAD"));
            if (hasUnread) {
                mailService.modifyThread(selectedThread.id, [], ["UNREAD"]).then(updatedData => {
                    if(onThreadUpdated) onThreadUpdated(updatedData);
                }).catch(err => {
                    console.error("Failed to mark as read", err);
                });
            }
        }
    }, [selectedThread?.id]);

    const handleActionClick = (actionName) => {
        setActionToConfirm(actionName);
        setIsConfirmModalOpen(true);
    };

    const handleConfirmAction = async () => {
        if (!actionToConfirm || !thread) return;
        
        try {
            let updatedData = null;
            let shouldDelete = false;

            if (actionToConfirm === 'Archive') {
                updatedData = await mailService.modifyThread(thread.id, [], ["INBOX"]);
                shouldDelete = true; // In Inbox view, archive means hide from Inbox
            } else if (actionToConfirm === 'Report spam') {
                let removeLabels = new Set(["INBOX"]);
                if (folder) removeLabels.add(folder.toUpperCase());
                removeLabels.delete("SPAM"); // Đảm bảo không vừa thêm vừa xóa
                
                updatedData = await mailService.modifyThread(thread.id, ["SPAM"], Array.from(removeLabels));
                shouldDelete = true;
            } else if (actionToConfirm === 'Delete') {
                await mailService.trashThread(thread.id);
                shouldDelete = true;
            } else if (actionToConfirm === 'Mark as read') {
                updatedData = await mailService.modifyThread(thread.id, [], ["UNREAD"]);
            } else if (actionToConfirm === 'Mark as unread') {
                updatedData = await mailService.modifyThread(thread.id, ["UNREAD"], []);
            } else if (actionToConfirm.startsWith('Move to')) {
                const folderName = actionToConfirm.replace('Move to ', '');
                const targetLabelId = folderName.toUpperCase();
                
                let removeLabels = new Set(["INBOX"]);
                if (folder) removeLabels.add(folder.toUpperCase());
                removeLabels.delete(targetLabelId); // Đảm bảo không vừa thêm vừa xóa
                
                updatedData = await mailService.modifyThread(thread.id, [targetLabelId], Array.from(removeLabels));
                shouldDelete = true;
            }

            if (shouldDelete) {
                if (onThreadDeleted) onThreadDeleted(thread.id);
            } else if (updatedData) {
                if (onThreadUpdated) onThreadUpdated(updatedData);
            }
        } catch (error) {
            console.error(`Failed to execute ${actionToConfirm}`, error);
        } finally {
            setIsConfirmModalOpen(false);
            setActionToConfirm(null);
        }
    };

    const handleCancelAction = () => {
        setIsConfirmModalOpen(false);
        setActionToConfirm(null);
    };

    const handleSummarize = () => {
        setIsSummarizing(true);
        // FIXME: Replace with real API call later
        setTimeout(() => {
            setSummary(`### Tóm tắt nội dung chính

Dưới đây là các điểm quan trọng liên quan đến cuộc thi trực tuyến sắp tới:

* **Trang phục:** Lịch sự, phù hợp với môi trường thi cử.
* **Cú pháp Zoom:** Đăng nhập 1 tài khoản/1 thiết bị theo cú pháp: \`GIP2026_SBD\` *(Ví dụ: GIP2026_001)*.
* **Thiết bị:** Ưu tiên **Laptop** và bật camera xuyên suốt *(không yêu cầu bật mic)*.
* **Không gian thi:** Yên tĩnh, ánh sáng đầy đủ, mạng ổn định. BTC có thể yêu cầu chụp hình điểm danh xác thực.

> **Lưu ý:** Mọi thắc mắc cần phản hồi lại email này **trước 19h00 ngày 01/04/2026**. Quá hạn BTC sẽ không tiếp nhận.`);
            setIsSummarizing(false);
        }, 1500); // Giả lập độ trễ API AI
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
                    <button 
                        onClick={handleSummarize}
                        disabled={isSummarizing || summary}
                        className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors disabled:opacity-50"
                    >
                        <Sparkles size={16} className={isSummarizing ? "text-muted-steel animate-pulse" : "text-spring-green"} />
                        {isSummarizing ? "Summarizing..." : "Summarize"}
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
                    {thread.messages?.some(msg => msg.labelIds?.includes('UNREAD')) ? (
                        <button 
                            onClick={() => handleActionClick('Mark as read')}
                            className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors"
                        >
                            <Mail size={16} />
                            <span>Mark as read</span>
                        </button>
                    ) : (
                        <button 
                            onClick={() => handleActionClick('Mark as unread')}
                            className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors"
                        >
                            <Mail size={16} />
                            <span>Mark as unread</span>
                        </button>
                    )}
                    
                    <div className="relative">
                        <button 
                            onClick={() => setIsMoveToDropdownOpen(!isMoveToDropdownOpen)}
                            className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors"
                        >
                            <FolderInput size={16} />
                            <span>Move to</span>
                        </button>

                        {isMoveToDropdownOpen && (
                            <div className="absolute right-0 mt-2 w-36 bg-pure-surface border border-whisper rounded-xl shadow-xl z-20 py-2 animate-in fade-in zoom-in-95 duration-100 origin-top-right ring-1 ring-black/5">
                                {[
                                    { name: 'Inbox', icon: Inbox },
                                    { name: 'Starred', icon: Star },
                                    { name: 'Important', icon: Bookmark }
                                ]
                                .filter(f => f.name.toLowerCase() !== folder?.toLowerCase())
                                .map((f) => {
                                    const Icon = f.icon;
                                    return (
                                        <button
                                            key={f.name}
                                            onClick={() => {
                                                handleActionClick(`Move to ${f.name}`);
                                                setIsMoveToDropdownOpen(false);
                                            }}
                                            className="w-full flex items-center gap-3 px-4 py-2.5 text-[14px] font-medium text-charcoal-ink hover:bg-canvas-gray transition-colors group"
                                        >
                                            <Icon size={16} className="text-muted-steel group-hover:text-primary-blue transition-colors" />
                                            <span className="group-hover:text-charcoal-ink">{f.name}</span>
                                        </button>
                                    );
                                })}
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Reading Pane */}
            <div className="flex-1 px-12 py-8 overflow-y-auto">
                <div className="max-w-[1000px] w-full mx-auto">
                    <h1 className={`text-2xl mb-8 font-semibold ${thread.subject ? 'text-charcoal-ink' : 'text-muted-steel italic'}`}>
                        {thread.subject || "(No subject)"}
                    </h1>

                    {/* AI Summary Section */}
                    {summary && (
                        <div className="mb-8 p-6 bg-spring-green/5 border border-spring-green/20 rounded-xl relative shadow-sm">
                            <button 
                                onClick={() => setSummary(null)}
                                className="absolute top-4 right-4 text-muted-steel hover:text-charcoal-ink transition-colors"
                            >
                                <X size={18} />
                            </button>
                            <div className="flex items-center gap-2 mb-3">
                                <Sparkles size={18} className="text-spring-green" />
                                <h3 className="font-semibold text-charcoal-ink">AI Summary</h3>
                            </div>
                            <div className="text-charcoal-ink/90 text-[14px] leading-relaxed">
                                <ReactMarkdown
                                    components={{
                                        h3: ({node, ...props}) => <h3 className="text-[15px] font-bold mt-2 mb-1" {...props} />,
                                        p: ({node, ...props}) => <p className="mb-2" {...props} />,
                                        ul: ({node, ...props}) => <ul className="list-disc pl-5 mb-2 space-y-1" {...props} />,
                                        li: ({node, ...props}) => <li {...props} />,
                                        strong: ({node, ...props}) => <strong className="font-bold text-charcoal-ink" {...props} />,
                                        em: ({node, ...props}) => <em className="italic text-charcoal-ink/70" {...props} />,
                                        code: ({node, ...props}) => <code className="bg-canvas-gray px-1.5 py-0.5 rounded text-[13px] text-primary-blue font-mono" {...props} />,
                                        blockquote: ({node, ...props}) => <blockquote className="border-l-4 border-spring-green/40 pl-3 py-1 my-3 bg-spring-green/5 text-charcoal-ink/80 italic rounded-r align-middle" {...props} />,
                                    }}
                                >
                                    {summary}
                                </ReactMarkdown>
                            </div>
                        </div>
                    )}

                    {/* Stack of Messages in the Thread */}
                    <div className="space-y-6">
                        {thread.messages &&
                            thread.messages.map((email) => (
                                <div
                                    key={email.id}
                                    className="bg-pure-surface rounded-xl p-6 border border-whisper shadow-sm"
                                >
                                    {/* Sender Info */}
                                    <div className="flex justify-between items-start mb-6 pt-2">
                                        <div className="flex items-start gap-3">
                                            <div className="w-10 h-10 shrink-0 mt-0.5">
                                                <img
                                                    src={`https://ui-avatars.com/api/?name=${encodeURIComponent(email.fromName || "Unknown")}&background=random&color=fff&rounded=true&bold=true`}
                                                    alt={email.fromName}
                                                    className="w-full h-full rounded-full"
                                                />
                                            </div>
                                            <div className="flex flex-col">
                                                <div className="flex items-baseline gap-1.5">
                                                    <span className="font-semibold text-charcoal-ink">
                                                        {email.fromName || "(Unknown)"}
                                                    </span>
                                                    <span className="text-[12px] text-muted-steel">
                                                        {email.fromEmail ? `<${email.fromEmail}>` : ""}
                                                    </span>
                                                </div>
                                                <div className="text-[12px] text-muted-steel mt-0.5 flex items-center gap-1">
                                                    <span>to</span>
                                                    <span className="text-charcoal-ink/80">
                                                        {!email.toName && !email.toEmail 
                                                            ? "me" 
                                                            : (email.toName && email.toName !== email.toEmail)
                                                                ? `${email.toName} <${email.toEmail}>`
                                                                : (email.toEmail || email.toName)
                                                        }
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="text-muted-steel text-sm mt-0.5">
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

                    {/* Reply / Forward Actions or Composer */}
                    {isReplying ? (
                        <div className="mt-8 mb-8 bg-pure-surface rounded-xl border border-spring-green/20 shadow-[0_4px_24px_rgba(0,0,0,0.06)] overflow-hidden ring-1 ring-spring-green/10">
                            {/* Composer Header */}
                            <div className="bg-[#f2faf6] px-6 py-4 border-b border-spring-green/10 flex items-center justify-between">
                                <div className="flex items-center gap-4">
                                    <div className="flex items-center gap-1.5 text-spring-green font-semibold text-[13px] tracking-wide">
                                        <Wand2 size={15} />
                                        <span>LLM Draft</span>
                                    </div>
                                    <div className="flex items-center gap-2 text-muted-steel/60 text-[12px] italic">
                                        <div className="w-1.5 h-1.5 rounded-full bg-spring-green animate-pulse"></div>
                                        AI Writing...
                                    </div>
                                </div>
                                <div className="text-[11px] font-bold tracking-widest text-[#8da69c]">
                                    FORMAL TONE
                                </div>
                            </div>
                            
                            {/* Editor Area */}
                            <div className="p-6">
                                <textarea
                                    autoFocus
                                    className="w-full min-h-[200px] text-charcoal-ink text-[15px] leading-relaxed resize-none focus:outline-none placeholder:text-muted-steel/50 bg-transparent"
                                    placeholder="Type your reply here..."
                                    value={replyContent}
                                    onChange={(e) => setReplyContent(e.target.value)}
                                />
                            </div>

                            {/* Composer Footer Actions */}
                            <div className="bg-canvas-gray/30 px-6 py-4 border-t border-whisper flex items-center justify-between">
                                <div className="flex items-center gap-2">
                                    <button 
                                        className="bg-spring-green hover:bg-[#00A86B] text-white px-6 py-2 rounded font-medium text-[14px] transition-colors shadow-sm"
                                        onClick={() => setIsReplying(false)}
                                    >
                                        Send
                                    </button>
                                    <button className="bg-[#006039] hover:bg-[#004f2f] text-white flex items-center gap-1.5 px-4 py-2 rounded font-medium text-[14px] transition-colors shadow-sm border border-[#006039]">
                                        <ListChecks size={16} />
                                        Insert
                                    </button>
                                </div>
                                <div className="flex items-center gap-2">
                                    <button className="flex items-center justify-center gap-2 px-4 py-2 border border-whisper rounded text-[14px] font-medium text-charcoal-ink bg-pure-surface hover:bg-canvas-gray transition-colors shadow-sm">
                                        <RefreshCw size={14} className="text-muted-steel" />
                                        Regenerate
                                    </button>
                                    <button 
                                        onClick={() => setIsReplying(false)}
                                        className="flex items-center justify-center p-2 text-muted-steel hover:text-rose-500 rounded bg-pure-surface hover:bg-rose-50 border border-transparent hover:border-rose-100 transition-colors"
                                        title="Discard draft"
                                    >
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="mt-8 flex gap-3 pb-8">
                            <button 
                                onClick={() => setIsReplying(true)}
                                className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-6 py-2.5 rounded-full bg-pure-surface font-medium hover:bg-canvas-gray transition-colors shadow-sm"
                            >
                                <Reply size={18} className="text-muted-steel" />
                                Reply
                            </button>
                            <button className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-6 py-2.5 rounded-full bg-pure-surface font-medium hover:bg-canvas-gray transition-colors shadow-sm">
                                <Forward size={18} className="text-muted-steel" />
                                Forward
                            </button>
                        </div>
                    )}
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
                                    className="px-4 py-2 rounded-lg font-medium text-charcoal-i k bg-canvas-gray hover:bg-whisper/50 transition-colors"
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
