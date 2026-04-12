import React, { useState } from 'react';
import { X, Mic, Paperclip, Send, Maximize2, Minimize2, Trash2 } from 'lucide-react';
import mailService from '../../service/mailService';

export default function ComposeModal({ isOpen, onClose }) {
    const [to, setTo] = useState('');
    const [cc, setCc] = useState('');
    const [bcc, setBcc] = useState('');
    const [showCcBcc, setShowCcBcc] = useState(false);
    const [subject, setSubject] = useState('');
    const [body, setBody] = useState('');
    const [isMinimized, setIsMinimized] = useState(false);
    const [isSending, setIsSending] = useState(false);

    if (!isOpen) return null;

    const handleSend = async () => {
        setIsSending(true);
        // FIXME: API call here when backend is ready
        // await mailService.sendEmail({ to, cc, bcc, subject, body });
        console.log("Sending email:", { to, cc, bcc, subject, body });
        setTimeout(() => {
            setIsSending(false);
            onClose();
        }, 1000); // Simulate network
    };

    if (isMinimized) {
        return (
            <div className="fixed bottom-0 right-16 w-80 bg-pure-surface shadow-[0_-4px_24px_rgba(0,0,0,0.1)] rounded-t-xl border border-whisper flex items-center justify-between px-4 py-3 z-50 cursor-pointer overflow-hidden group"
                 onClick={() => setIsMinimized(false)}>
                <span className="font-semibold text-charcoal-ink truncate pr-4">
                    {subject || 'New Message'}
                </span>
                <div className="flex items-center gap-2 text-muted-steel">
                    <button className="hover:bg-canvas-gray p-1 rounded transition-colors group-hover:text-charcoal-ink">
                        <Maximize2 size={16} />
                    </button>
                    <button className="hover:bg-canvas-gray p-1 rounded transition-colors group-hover:text-charcoal-ink"
                            onClick={(e) => { e.stopPropagation(); onClose(); }}>
                        <X size={16} />
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="fixed bottom-0 right-16 w-[640px] bg-pure-surface shadow-[0_-4px_24px_rgba(0,0,0,0.1)] rounded-t-xl border border-whisper flex flex-col z-50 overflow-hidden animate-in slide-in-from-bottom-5 duration-200">
            {/* Header */}
            <div className="flex items-center justify-between bg-charcoal-ink text-white px-4 py-3 cursor-pointer"
                 onClick={() => setIsMinimized(true)}>
                <span className="font-medium text-sm">New Message</span>
                <div className="flex items-center gap-2">
                    <button className="hover:bg-white/20 p-1 rounded transition-colors"
                            onClick={(e) => { e.stopPropagation(); setIsMinimized(true); }}>
                        <Minimize2 size={16} />
                    </button>
                    <button className="hover:bg-white/20 p-1 rounded transition-colors"
                            onClick={(e) => { e.stopPropagation(); onClose(); }}>
                        <X size={16} />
                    </button>
                </div>
            </div>

            {/* Form Fields */}
            <div className="flex flex-col px-4 py-2 border-b border-whisper text-[14px]">
                <div className="flex items-center border-b border-whisper/50 py-2">
                    <span className="text-muted-steel w-12 shrink-0">To</span>
                    <input 
                        type="email" 
                        value={to}
                        onChange={(e) => setTo(e.target.value)}
                        className="flex-1 bg-transparent outline-none text-charcoal-ink placeholder-muted-steel"
                        placeholder="recipient@example.com"
                    />
                    {!showCcBcc && (
                        <button 
                            className="text-muted-steel hover:text-charcoal-ink text-xs font-medium px-2"
                            onClick={() => setShowCcBcc(true)}
                        >
                            Cc Bcc
                        </button>
                    )}
                </div>
                {showCcBcc && (
                    <>
                        <div className="flex items-center border-b border-whisper/50 py-2">
                            <span className="text-muted-steel w-12 shrink-0">Cc</span>
                            <input 
                                type="email" 
                                value={cc}
                                onChange={(e) => setCc(e.target.value)}
                                className="flex-1 bg-transparent outline-none text-charcoal-ink placeholder-muted-steel"
                            />
                        </div>
                        <div className="flex items-center border-b border-whisper/50 py-2">
                            <span className="text-muted-steel w-12 shrink-0">Bcc</span>
                            <input 
                                type="email" 
                                value={bcc}
                                onChange={(e) => setBcc(e.target.value)}
                                className="flex-1 bg-transparent outline-none text-charcoal-ink placeholder-muted-steel"
                            />
                        </div>
                    </>
                )}
                <div className="flex py-2">
                    <input 
                        type="text" 
                        value={subject}
                        onChange={(e) => setSubject(e.target.value)}
                        className="flex-1 bg-transparent outline-none text-charcoal-ink font-semibold placeholder-muted-steel/70"
                        placeholder="Subject"
                    />
                </div>
            </div>

            {/* Body */}
            <div className="flex-1 p-4 min-h-[400px]">
                <textarea 
                    className="w-full h-full resize-none outline-none text-charcoal-ink text-[14px] bg-transparent"
                    placeholder="Write something..."
                    value={body}
                    onChange={(e) => setBody(e.target.value)}
                />
            </div>

            {/* Actions */}
            <div className="flex items-center justify-between px-4 py-3 border-t border-whisper bg-canvas-gray/50">
                <button 
                    onClick={handleSend}
                    disabled={isSending || !to}
                    className="flex items-center gap-2 bg-spring-green hover:bg-spring-green/90 text-white px-6 py-2 rounded-lg font-medium shadow-sm transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    {isSending ? (
                        <>Sending...</>
                    ) : (
                        <>
                            <span>Send</span>
                            <Send size={16} />
                        </>
                    )}
                </button>
                
                <div className="flex items-center gap-2 text-muted-steel">
                    <button className="p-2 hover:bg-black/5 rounded transition-colors" title="Attach files">
                        <Paperclip size={18} />
                    </button>
                    <button className="p-2 hover:bg-black/5 rounded transition-colors text-primary-blue hover:text-primary-blue" title="Smart dictate">
                        <Mic size={18} />
                    </button>
                    <button className="p-2 hover:bg-black/5 rounded transition-colors ml-2" onClick={onClose} title="Discard draft">
                        <Trash2 size={18} />
                    </button>
                </div>
            </div>
        </div>
    );
}