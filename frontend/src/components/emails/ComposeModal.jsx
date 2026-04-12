import React, { useState, useRef } from 'react';
import { X, Mic, Paperclip, Send, Maximize2, Minimize2, Trash2, CheckCircle2 } from 'lucide-react';
import mailService from '../../service/mailService';
import toast from 'react-hot-toast';

export default function ComposeModal({ isOpen, onClose }) {
    const [to, setTo] = useState('');
    const [cc, setCc] = useState('');
    const [bcc, setBcc] = useState('');
    const [showCcBcc, setShowCcBcc] = useState(false);
    const [subject, setSubject] = useState('');
    const [body, setBody] = useState('');
    const [attachments, setAttachments] = useState([]);
    const [isMinimized, setIsMinimized] = useState(false);
    const [isSending, setIsSending] = useState(false);
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const fileInputRef = useRef(null);

    if (!isOpen) return null;

    const handleActualSend = async () => {
        setIsSending(true);
        
        // Get actual valid files
        const validFiles = attachments.filter(a => a.status === 'success').map(a => a.file);
        const toastId = toast.loading("Sending your email...");
        
        try {
            await mailService.sendEmail({ to, cc, bcc, subject, body, attachments: validFiles });
            toast.success("Email sent successfully!", { id: toastId });
        } catch (error) {
            console.error("Error sending email:", error);
            toast.error("Failed to send email. Please try again.", { id: toastId });
        } finally {
            setIsSending(false);
        }
    };

    const handleSendClick = () => {
        setShowConfirmModal(true);
    };

    const handleConfirmSend = () => {
        setShowConfirmModal(false);
        handleActualSend();
        onClose(); // Close compose modal immediately
    };

    const addFiles = (files) => {
        if (!files || files.length === 0) return;
        const newFiles = Array.from(files).map(file => ({
            id: Math.random().toString(36).substr(2, 9),
            file,
            progress: 0,
            status: 'uploading'
        }));
        
        setAttachments(prev => [...prev, ...newFiles]);

        // Simulating upload progress for smooth UI like Gmail
        newFiles.forEach(att => {
            let p = 0;
            const interval = setInterval(() => {
                p += Math.floor(Math.random() * 20) + 10;
                if (p >= 100) {
                    p = 100;
                    clearInterval(interval);
                    setAttachments(current => current.map(item => item.id === att.id ? { ...item, progress: 100, status: 'success' } : item));
                } else {
                    setAttachments(current => current.map(item => item.id === att.id ? { ...item, progress: p } : item));
                }
            }, 300);
        });
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
            {attachments.length > 0 && (
                <div className="flex flex-wrap gap-2 px-4 py-2 border-t border-whisper bg-canvas-gray/20">
                    {attachments.map((item) => (
                        <div key={item.id} className="relative flex items-center gap-1.5 bg-pure-surface border border-whisper text-charcoal-ink text-xs px-2 py-1.5 rounded-md shadow-sm overflow-hidden min-w-[200px]">
                            {item.status === 'uploading' && (
                                <div className="absolute bottom-0 left-0 h-[2px] bg-primary-blue transition-all duration-300" style={{ width: `${item.progress}%` }} />
                            )}
                            <Paperclip size={12} className="text-muted-steel shrink-0" />
                            <span className="truncate flex-1 font-medium" title={item.file.name}>{item.file.name}</span>
                            <span className="text-[10px] text-muted-steel ml-1 shrink-0">
                                {item.file.size > 1024 * 1024 ? `${(item.file.size / (1024 * 1024)).toFixed(1)}MB` : `${Math.round(item.file.size / 1024)}KB`}
                            </span>
                            
                            {item.status === 'uploading' ? (
                                <span className="text-[10px] text-primary-blue font-semibold w-[24px] text-right">{item.progress}%</span>
                            ) : (
                                <button 
                                    className="ml-1 hover:bg-canvas-gray p-0.5 rounded text-muted-steel hover:text-red-500 transition-colors shrink-0" 
                                    onClick={() => setAttachments(prev => prev.filter(a => a.id !== item.id))}
                                    title="Remove attachment"
                                >
                                    <X size={14} />
                                </button>
                            )}
                        </div>
                    ))}
                </div>
            )}
            <div className="flex items-center justify-between px-4 py-3 border-t border-whisper bg-canvas-gray/50">
                <button 
                    onClick={handleSendClick}
                    disabled={isSending || !to || attachments.some(a => a.status === 'uploading')}
                    className="flex items-center gap-2 bg-spring-green hover:bg-spring-green/90 text-white px-6 py-2 rounded-lg font-medium shadow-sm transition-all disabled:opacity-50 disabled:cursor-not-allowed relative"
                >
                    <span>Send</span>
                    <Send size={16} />
                </button>
                
                <div className="flex items-center gap-2 text-muted-steel">
                    <input 
                        type="file" 
                        multiple 
                        className="hidden" 
                        ref={fileInputRef} 
                        onChange={(e) => {
                            addFiles(e.target.files);
                            if (fileInputRef.current) {
                                fileInputRef.current.value = '';
                            }
                        }}
                    />
                    <button type="button" className="p-2 hover:bg-black/5 rounded transition-colors" title="Attach files" onClick={(e) => { e.preventDefault(); fileInputRef.current?.click(); }}>
                        <Paperclip size={18} />
                    </button>
                    <button type="button" className="p-2 hover:bg-black/5 rounded transition-colors text-primary-blue hover:text-primary-blue" title="Smart dictate" onClick={(e) => e.preventDefault()}>
                        <Mic size={18} />
                    </button>
                    <button type="button" className="p-2 hover:bg-black/5 rounded transition-colors ml-2" onClick={(e) => { e.preventDefault(); onClose(); }} title="Discard draft">
                        <Trash2 size={18} />
                    </button>
                </div>
            </div>

            {/* Confirm Modal Overlay */}
            {showConfirmModal && (
                <div className="fixed inset-0 z-[60] flex items-center justify-center pointer-events-auto bg-charcoal-ink/30 backdrop-blur-sm transition-all duration-300">
                    <div className="bg-pure-surface rounded-2xl shadow-xl w-[380px] overflow-hidden animate-in fade-in zoom-in-95 duration-200">
                        <div className="p-6 text-center">
                            <div className="w-12 h-12 rounded-full bg-spring-green/10 flex items-center justify-center mx-auto mb-4">
                                <Send size={22} className="text-spring-green ml-1" />
                            </div>
                            <h3 className="font-bold text-lg text-charcoal-ink mb-2">
                                Send Email
                            </h3>
                            <p className="text-[14px] text-muted-steel leading-relaxed">
                                Are you sure you want to send this email to <br/>
                                <span className="font-semibold text-charcoal-ink">{to}</span>?
                            </p>
                        </div>
                        <div className="px-6 py-4 bg-canvas-gray flex justify-center gap-3 border-t border-whisper">
                            <button 
                                className="flex-1 py-2.5 text-[14px] text-charcoal-ink bg-pure-surface border border-whisper hover:bg-whisper/50 font-semibold rounded-xl transition-colors"
                                onClick={() => setShowConfirmModal(false)}
                            >
                                Cancel
                            </button>
                            <button 
                                className="flex-1 py-2.5 text-[14px] bg-spring-green hover:bg-spring-green-hover text-white font-semibold rounded-xl shadow-sm transition-colors"
                                onClick={handleConfirmSend}
                            >
                                Confirm
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}