import React, { useState, useRef, useEffect } from 'react';
import { X, Paperclip, Send, Trash2, Edit2, Eye, Sparkles } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import { marked } from 'marked';
import mailService from '../../services/mailService';
import aiService from '../../services/aiService';
import toast from 'react-hot-toast';

export default function ReplyBox({ thread, message, type = 'reply', onDiscard, onSent }) {
    const [to, setTo] = useState('');
    const [cc, setCc] = useState('');
    const [bcc, setBcc] = useState('');
    const [showCcBcc, setShowCcBcc] = useState(false);
    const [subject, setSubject] = useState('');
    const [body, setBody] = useState('');
    const [attachments, setAttachments] = useState([]);
    const [isPreview, setIsPreview] = useState(false);
    const [editorType, setEditorType] = useState('text');
    const [isSending, setIsSending] = useState(false);
    const [isGeneratingDraft, setIsGeneratingDraft] = useState(false);
    const fileInputRef = useRef(null);

    useEffect(() => {
        if (thread && thread.messages && thread.messages.length > 0) {
            const defaultMsg = thread.messages[thread.messages.length - 1];
            const targetMsg = message || defaultMsg;
            
            if (type === 'reply') {
                setTo(targetMsg.fromEmail || '');
                let subj = thread.subject || '';
                if (!subj.toLowerCase().startsWith('re:')) {
                    subj = `Re: ${subj}`;
                }
                setSubject(subj);
                setEditorType('markdown');
                
                setEditorType('text');
                setBody('');
            } else if (type === 'forward') {
                setTo(''); // Need to enter recipient
                let subj = thread.subject || '';
                if (!subj.toLowerCase().startsWith('fwd:')) {
                    subj = `Fwd: ${subj}`;
                }
                setSubject(subj);
                
                // Add forwarded message content to body
                const forwardHeader = `\n\n---------- Forwarded message ---------\nFrom: <${targetMsg.fromEmail || ''}>\nDate: ${new Date(targetMsg.internalDate).toLocaleString()}\nSubject: ${thread.subject}\n\n`;
                
                let emailContent = '';
                if (targetMsg.bodyHtml) {
                    let htmlStr = targetMsg.bodyHtml;
                    htmlStr = htmlStr.replace(/<style[^>]*>[\s\S]*?<\/style>/gi, '');
                    htmlStr = htmlStr.replace(/<script[^>]*>[\s\S]*?<\/script>/gi, '');
                    htmlStr = htmlStr.replace(/<title[^>]*>[\s\S]*?<\/title>/gi, '');
                    htmlStr = htmlStr.replace(/<br\s*\/?>/gi, '\n');
                    htmlStr = htmlStr.replace(/<\/(p|div|h[1-6]|ul|ol|table|blockquote|pre)>/gi, '\n\n');
                    htmlStr = htmlStr.replace(/<\/(li|tr)>/gi, '\n');
                    htmlStr = htmlStr.replace(/<li[^>]*>/gi, '\n  • ');
                    
                    const doc = new DOMParser().parseFromString(htmlStr, 'text/html');
                    emailContent = doc.documentElement.textContent || '';
                    emailContent = emailContent.replace(/\n\s*\n\s*\n+/g, '\n\n');
                } else if (targetMsg.bodyText) {
                    emailContent = targetMsg.bodyText;
                } else {
                    emailContent = targetMsg.snippet || '';
                }

                setBody(forwardHeader + emailContent.trim());
            }
        }
    }, [thread, message, type]);

    const handleGenerateDraft = async () => {
        try {
            setIsGeneratingDraft(true);
            const defaultMsg = thread.messages && thread.messages.length > 0 ? thread.messages[thread.messages.length - 1] : null;
            const targetMsg = message || defaultMsg;
            const contentToDraft = targetMsg?.bodyText || targetMsg?.bodyHtml || thread?.snippet || '';
            
            if (contentToDraft) {
                const response = await aiService.generateDraft(thread.id, contentToDraft, editorType);
                setBody(response.draftContent || '');
            }
        } catch (error) {
            toast.error('Failed to generate AI draft');
            console.error('Failed to generate AI draft:', error);
        } finally {
            setIsGeneratingDraft(false);
        }
    };

    const handleActualSend = async () => {
        setIsSending(true);
        
        let finalBody = body;
        let isHtml = editorType === 'html';
        
        if (editorType === 'markdown') {
            const parsedHtml = await marked.parse(body);
            finalBody = `<div style="font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace; font-size: 14px; line-height: 1.6; color: #333;">${parsedHtml}</div>`;
            isHtml = true;
        }
        
        const validFiles = attachments.filter(a => a.status === 'success').map(a => a.file);
        const toastId = toast.loading(`Sending your ${type}...`);
        
        try {
            const defaultMsg = thread.messages && thread.messages.length > 0 ? thread.messages[thread.messages.length - 1] : null;
            const targetMsg = message || defaultMsg;
            
            await mailService.sendEmail({ 
                to, 
                cc, 
                bcc, 
                subject, 
                body: finalBody, 
                isHtml, 
                attachments: validFiles,
                threadId: thread.id,
                inReplyTo: type === 'reply' && targetMsg ? targetMsg.messageIdHeader : undefined
            });
            toast.success(`${type === 'forward' ? 'Forwarded' : 'Reply sent'} successfully!`, { id: toastId });
            if (onSent) onSent();
        } catch (error) {
            console.error(`Error sending ${type}:`, error);
            toast.error(`Failed to send ${type}. Please try again.`, { id: toastId });
        } finally {
            setIsSending(false);
        }
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

    return (
        <div className="bg-pure-surface border border-whisper shadow-sm rounded-xl overflow-hidden flex flex-col">
            {/* Form Fields Header */}
            <div className="flex flex-col px-4 py-2 border-b border-whisper text-[14px]">
                <div className="flex items-center border-b border-whisper/50 py-2">
                    <span className="text-muted-steel w-12 shrink-0">To</span>
                    <input 
                        type="email" 
                        value={to}
                        onChange={(e) => setTo(e.target.value)}
                        className="flex-1 bg-transparent outline-none text-charcoal-ink placeholder-muted-steel font-medium"
                        placeholder="recipient@example.com"
                    />
                    {!showCcBcc && (
                        <button 
                            className="text-muted-steel hover:text-charcoal-ink text-xs font-medium px-2 flex-shrink-0"
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
                <div className="flex py-2 items-center justify-between">
                    <div className="flex-1 flex gap-2">
                        <span className="text-muted-steel font-medium whitespace-nowrap">Subject:</span>
                        <input 
                            type="text" 
                            value={subject}
                            onChange={(e) => setSubject(e.target.value)}
                            className="flex-1 bg-transparent outline-none text-charcoal-ink placeholder-muted-steel/70"
                            placeholder="Subject"
                        />
                    </div>
                    <div className="flex items-center gap-2 shrink-0 ml-4">
                        <select
                            value={editorType}
                            onChange={(e) => {
                                setEditorType(e.target.value);
                                if (e.target.value === 'text') setIsPreview(false);
                            }}
                            className="bg-canvas-gray border border-whisper text-muted-steel hover:text-charcoal-ink text-xs font-medium rounded-md px-2 py-1 outline-none cursor-pointer transition-colors"
                        >
                            <option value="text">Text</option>
                            <option value="markdown">Markdown</option>
                            <option value="html">HTML</option>
                        </select>
                        {editorType !== 'text' && (
                            <button
                                onClick={() => setIsPreview(!isPreview)}
                                className="flex items-center gap-1.5 px-3 py-1 text-xs font-medium rounded-full bg-canvas-gray hover:bg-whisper text-muted-steel hover:text-charcoal-ink transition-colors border border-whisper"
                            >
                                {isPreview ? (
                                    <>
                                        <Edit2 size={14} />
                                        <span>Edit</span>
                                    </>
                                ) : (
                                    <>
                                        <Eye size={14} />
                                        <span>Preview</span>
                                    </>
                                )}
                            </button>
                        )}
                    </div>
                </div>
            </div>

            {/* Editor Area */}
            <div className="p-4 min-h-[250px] max-h-[500px] overflow-y-auto custom-scrollbar bg-canvas-gray/10">
                {isPreview ? (
                    <div className="w-full h-full p-2 text-charcoal-ink/90 text-[14px] leading-relaxed">
                        {body ? (
                            editorType === 'markdown' ? (
                                <ReactMarkdown
                                    components={{
                                        h1: ({node, ...props}) => <h1 className="text-2xl font-bold mt-4 mb-2" {...props} />,
                                        h2: ({node, ...props}) => <h2 className="text-xl font-bold mt-3 mb-2" {...props} />,
                                        h3: ({node, ...props}) => <h3 className="text-[15px] font-bold mt-2 mb-1" {...props} />,
                                        p: ({node, ...props}) => <p className="mb-2" {...props} />,
                                        ul: ({node, ...props}) => <ul className="list-disc pl-5 mb-2 space-y-1" {...props} />,
                                        ol: ({node, ...props}) => <ol className="list-decimal pl-5 mb-2 space-y-1" {...props} />,
                                        li: ({node, ...props}) => <li {...props} />,
                                        strong: ({node, ...props}) => <strong className="font-bold text-charcoal-ink" {...props} />,
                                        em: ({node, ...props}) => <em className="italic text-charcoal-ink/70" {...props} />,
                                        blockquote: ({node, ...props}) => <blockquote className="border-l-4 border-whisper pl-4 py-1 my-2 italic text-muted-steel bg-canvas-gray/30 rounded-r" {...props} />,
                                        pre: ({node, ...props}) => <pre className="bg-charcoal-ink text-whisper p-4 rounded-lg overflow-x-auto my-3 text-[13px] font-mono shadow-sm [&_code]:bg-transparent [&_code]:text-inherit [&_code]:p-0 [&_code]:m-0" {...props} />,
                                        code: ({node, className, ...props}) => <code className={`${className || ''} bg-red-50 text-red-500 px-1.5 py-0.5 rounded text-[13px] font-mono mx-0.5`} {...props} />,
                                        a: ({node, ...props}) => <a className="text-blue-500 hover:text-blue-600 hover:underline cursor-pointer" target="_blank" rel="noopener noreferrer" {...props} />,
                                    }}
                                >
                                    {body}
                                </ReactMarkdown>
                            ) : (
                                <div dangerouslySetInnerHTML={{ __html: body }} />
                            )
                        ) : (
                            <span className="text-muted-steel italic">Nothing to preview</span>
                        )}
                    </div>
                ) : (
                    <textarea 
                        disabled={isGeneratingDraft}
                        className={`w-full h-full min-h-[200px] resize-none outline-none text-charcoal-ink text-[14px] bg-transparent ${editorType === 'text' ? '' : 'font-mono'}`}
                        placeholder={isGeneratingDraft ? 'Đang tạo nháp AI (vui lòng chờ)...' : editorType === 'markdown' ? 'Write your reply using Markdown...' : editorType === 'html' ? '<html>\n  <body>\n    Write your reply using HTML...\n  </body>\n</html>' : 'Type your reply here...'}
                        value={body}
                        onChange={(e) => setBody(e.target.value)}
                    />
                )}
            </div>

            {/* Attachments Section */}
            {attachments.length > 0 && (
                <div className="flex flex-wrap gap-2 px-4 py-3 border-t border-whisper bg-pure-surface">
                    {attachments.map((item) => (
                        <div key={item.id} className="relative flex items-center gap-1.5 bg-canvas-gray border border-whisper text-charcoal-ink text-xs px-2 py-1.5 rounded-md shadow-sm overflow-hidden min-w-[200px]">
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
                                    className="ml-1 hover:bg-whisper/80 p-0.5 rounded text-muted-steel hover:text-red-500 transition-colors shrink-0" 
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

            {/* Footer Actions */}
            <div className="bg-canvas-gray/30 px-4 py-3 border-t border-whisper flex items-center justify-between shrink-0">
                <div className="flex gap-2">
                    <button 
                        onClick={handleActualSend}
                        disabled={isSending || isGeneratingDraft || !to || attachments.some(a => a.status === 'uploading')}
                        className="flex items-center gap-2 bg-spring-green hover:bg-spring-green/90 text-white px-6 py-2 rounded-lg font-medium shadow-sm transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        <span>{isSending ? 'Sending...' : 'Send'}</span>
                    </button>
                    
                    {type === 'reply' && (
                        <button
                            onClick={handleGenerateDraft}
                            disabled={isSending || isGeneratingDraft || !to}
                            title="Generate AI Reply Draft"
                            className="flex items-center gap-2 bg-indigo-50 border border-indigo-100 hover:bg-indigo-100 text-indigo-600 px-4 py-2 rounded-lg font-medium shadow-sm transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            <Sparkles size={16} />
                            <span>{isGeneratingDraft ? 'AI is drafting...' : 'AI Gen Draft'}</span>
                        </button>
                    )}
                </div>
                
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
                    <button type="button" className="p-2 hover:bg-pure-surface hover:shadow-sm rounded-lg transition-all" title="Attach files" onClick={() => fileInputRef.current?.click()}>
                        <Paperclip size={18} />
                    </button>
                    <button type="button" className="p-2 hover:bg-rose-50 hover:text-rose-500 hover:shadow-sm rounded-lg transition-all ml-2" onClick={onDiscard} title="Discard draft">
                        <Trash2 size={18} />
                    </button>
                </div>
            </div>
        </div>
    );
}
