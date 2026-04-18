import React, { useState, useEffect } from 'react';
import { Calendar, MapPin, Clock, ChevronRight, MessageSquare, Sparkles, BrainCircuit, RefreshCw } from 'lucide-react';
import { aiService, EventDto } from '../services/aiService';
import ThreadItem from '../components/emails/ThreadItem';
import EmailReader from '../components/emails/EmailReader';
import { useThreadDetail } from '../hooks/useThreadDetail';
import { Thread } from '../types/mail';
import ReactMarkdown from 'react-markdown';
import { X } from 'lucide-react';

export default function EventPage() {
    const [events, setEvents] = useState<EventDto[]>([]);
    const [summary, setSummary] = useState<string>('');
    const [relatedEmails, setRelatedEmails] = useState<Thread[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedThreadId, setSelectedThreadId] = useState<string | null>(null);
    const [isClosing, setIsClosing] = useState(false);

    const {
        selectedThreadData,
        isLoadingDetail,
        updateThreadDataLocally,
        clearThreadLocally
    } = useThreadDetail(selectedThreadId);

    const fetchEvents = async () => {
        setIsLoading(true);
        try {
            const response = await aiService.getUpcomingEvents();
            setEvents(response.aiResult.events);
            setSummary(response.aiResult.summary);
            setRelatedEmails(response.relatedEmails);
        } catch (error) {
            console.error("Failed to fetch events:", error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchEvents();
    }, []);

    const handleThreadUpdated = (updatedData: any) => {
        updateThreadDataLocally(updatedData);
    };

    const handleThreadDeleted = (deletedId: string) => {
        handleClosePanel();
        setRelatedEmails(prev => prev.filter(t => t.id !== deletedId));
    };

    const handleClosePanel = () => {
        setIsClosing(true);
        setTimeout(() => {
            setSelectedThreadId(null);
            setIsClosing(false);
        }, 250); // Matches the duration of animate-slide-out-right
    };

    if (isLoading) {
        return (
            <div className="flex-1 flex items-center justify-center bg-canvas-gray">
                <div className="flex flex-col items-center gap-4">
                    <div className="w-12 h-12 border-4 border-spring-green border-t-transparent rounded-full animate-spin"></div>
                    <p className="text-muted-steel font-medium animate-pulse">Analyzing your inbox for upcoming events...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="flex-1 flex flex-col h-full overflow-hidden bg-canvas-gray">
            {/* Header */}
            <header className="bg-pure-surface border-b border-whisper px-8 py-6 flex justify-between items-center shrink-0">
                <div className="flex items-center gap-4">
                    <div className="bg-spring-green/10 p-3 rounded-2xl">
                        <Calendar className="text-spring-green w-6 h-6" />
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold text-charcoal-ink tracking-tight">Intelligence Dashboard</h1>
                        <p className="text-sm text-muted-steel">AI-powered event extraction and conversation analysis</p>
                    </div>
                </div>
                <button 
                    onClick={fetchEvents}
                    className="flex items-center gap-2 px-4 py-2 bg-white border border-whisper rounded-xl text-sm font-semibold text-charcoal-ink hover:bg-canvas-gray transition-all shadow-sm active:scale-95"
                >
                    <RefreshCw className="w-4 h-4" />
                    Refresh Analysis
                </button>
            </header>

            {/* Scrollable Content Area */}
            <div className="flex-1 overflow-y-auto custom-scrollbar">
                <div className="max-w-[1600px] mx-auto p-8 space-y-8">
                    
                    {/* Upcoming Events List */}
                    <div className="space-y-4">
                        <div className="flex items-center gap-2 px-2">
                            <BrainCircuit className="w-5 h-5 text-spring-green" />
                            <h2 className="text-lg font-bold text-charcoal-ink">Detected Events</h2>
                            <span className="bg-spring-green/10 text-spring-green text-xs font-bold px-2 py-0.5 rounded-full ml-auto">
                                {events.length} Found
                            </span>
                        </div>
                        
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                            {events.map((event, index) => (
                                <div key={index} className="group bg-pure-surface p-5 rounded-2xl border border-whisper shadow-sm hover:shadow-md hover:border-spring-green/30 transition-all cursor-default relative overflow-hidden">
                                    <div className="absolute top-0 right-0 p-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                        <div className="bg-spring-green/10 text-spring-green text-[10px] font-bold px-2 py-1 rounded-lg uppercase tracking-wider">
                                            {event.status}
                                        </div>
                                    </div>
                                    <h3 className="font-bold text-charcoal-ink mb-3 pr-16 line-clamp-1">{event.title}</h3>
                                    <div className="space-y-2.5">
                                        <div className="flex items-center gap-2.5 text-sm text-muted-steel">
                                            <div className="p-1.5 bg-canvas-gray rounded-lg">
                                                <Clock className="w-3.5 h-3.5" />
                                            </div>
                                            <span className="font-medium">{event.datetime}</span>
                                        </div>
                                        <div className="flex items-center gap-2.5 text-sm text-muted-steel">
                                            <div className="p-1.5 bg-canvas-gray rounded-lg">
                                                <MapPin className="w-3.5 h-3.5" />
                                            </div>
                                            <span className="truncate font-medium">{event.location}</span>
                                        </div>
                                    </div>
                                    {event.description && (
                                        <p className="mt-4 text-[13px] text-muted-steel leading-relaxed bg-canvas-gray/50 p-3 rounded-xl border border-whisper/50">
                                            {event.description}
                                        </p>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* AI Summary Card */}
                    <div className="space-y-4">
                        <div className="flex items-center gap-2 px-2">
                            <Sparkles className="w-5 h-5 text-spring-green" />
                            <h2 className="text-lg font-bold text-charcoal-ink">AI Insights</h2>
                        </div>
                        <div className="bg-pure-surface p-6 rounded-2xl border border-whisper shadow-sm flex flex-col">
                            <div className="text-charcoal-ink leading-relaxed">
                                {summary ? (
                                    <ReactMarkdown
                                        components={{
                                            h3: ({node, ...props}) => <h3 className="text-[16px] font-bold mt-4 mb-2 text-charcoal-ink" {...props} />,
                                            p: ({node, ...props}) => <p className="mb-3 text-[14px]" {...props} />,
                                            ul: ({node, ...props}) => <ul className="list-disc pl-5 mb-3 space-y-1.5 text-[14px]" {...props} />,
                                            li: ({node, ...props}) => <li {...props} />,
                                            strong: ({node, ...props}) => <strong className="font-bold text-charcoal-ink" {...props} />,
                                            em: ({node, ...props}) => <em className="italic text-charcoal-ink/70" {...props} />,
                                            code: ({node, ...props}) => <code className="bg-canvas-gray px-1.5 py-0.5 rounded text-[13px] text-primary-blue font-mono" {...props} />,
                                            blockquote: ({node, ...props}) => <blockquote className="border-l-4 border-spring-green/40 pl-3 py-1 my-3 bg-spring-green/5 text-charcoal-ink/80 italic rounded-r" {...props} />,
                                        }}
                                    >
                                        {summary}
                                    </ReactMarkdown>
                                ) : (
                                    <p className="text-muted-steel italic">No specific insights generated for these events.</p>
                                )}
                            </div>
                        </div>
                    </div>

                    {/* Source Conversations List */}
                    {relatedEmails.length > 0 && (
                        <div className="space-y-4">
                            <div className="flex items-center gap-2 px-2">
                                <MessageSquare className="w-5 h-5 text-spring-green" />
                                <h2 className="text-lg font-bold text-charcoal-ink">Source Conversations</h2>
                                <p className="text-sm text-muted-steel ml-2 font-medium">Emails that triggered these event extractions</p>
                            </div>

                            <div className="bg-pure-surface rounded-2xl border border-whisper shadow-sm overflow-hidden divide-y divide-whisper/50">
                                {relatedEmails.map((thread) => (
                                    <ThreadItem 
                                        key={thread.id}
                                        thread={thread}
                                        isSelected={selectedThreadId === thread.id}
                                        onClick={() => setSelectedThreadId(thread.id)}
                                    />
                                ))}
                            </div>
                        </div>
                    )}
                </div>

                {/* Side Panel for Thread Details */}
                {selectedThreadId && (
                    <div className="fixed inset-0 z-[100] flex justify-end">
                        {/* Backdrop */}
                        <div 
                            className={`absolute inset-0 bg-transparent transition-opacity duration-300 ${isClosing ? 'opacity-0' : 'opacity-100'}`}
                            onClick={handleClosePanel}
                        />
                        
                        {/* Panel Content */}
                        <div className={`relative w-full max-w-[50%] bg-white shadow-2xl h-full flex flex-col border-l border-whisper ${isClosing ? 'animate-slide-out-right' : 'animate-slide-in-right'}`}>
                            <div className="flex items-center justify-between p-4 border-b border-whisper bg-white z-10">
                                <div className="flex items-center gap-3">
                                    <div className="bg-spring-green/10 p-2 rounded-xl">
                                        <MessageSquare className="w-5 h-5 text-spring-green" />
                                    </div>
                                    <h3 className="text-lg font-bold text-charcoal-ink">Conversation Context</h3>
                                </div>
                                <button 
                                    onClick={handleClosePanel}
                                    className="p-2 hover:bg-canvas-gray rounded-full transition-colors text-muted-steel hover:text-charcoal-ink"
                                >
                                    <X className="w-5 h-5" />
                                </button>
                            </div>
                            
                            <div className="flex-1 overflow-hidden flex flex-col bg-canvas-gray/20">
                                <EmailReader 
                                    folder="all"
                                    selectedThread={selectedThreadData}
                                    isLoading={isLoadingDetail}
                                    onThreadUpdated={handleThreadUpdated}
                                    onThreadDeleted={handleThreadDeleted}
                                />
                            </div>
                        </div>
                    </div>
                )}
                
                {/* Footer Spacer */}
                <div className="h-12"></div>
            </div>
        </div>
    );
}
