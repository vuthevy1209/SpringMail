import React, { useEffect, useState } from 'react';
import { aiService, EventDto } from '../services/aiService';
import { Loader2, AlertCircle } from 'lucide-react';

export default function EventPage() {
    const [events, setEvents] = useState<EventDto[]>([]);
    const [rawAnalysis, setRawAnalysis] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchEvents = async () => {
            try {
                setIsLoading(true);
                setError(null);
                const response = await aiService.extractUpcomingEvents();
                setEvents(response.events);
                setRawAnalysis(response.rawAnalysis);
            } catch (err) {
                console.error('Failed to load events:', err);
                setError('Unable to load upcoming events. Please try again later.');
            } finally {
                setIsLoading(false);
            }
        };

        fetchEvents();
    }, []);

    return (
        <div className="flex-1 bg-white h-full overflow-y-auto">
            <div className="p-8">
                <div className="flex items-center justify-between mb-8">
                    <h1 className="text-2xl font-bold text-charcoal-ink">Upcoming Events</h1>
                </div>
                
                {isLoading ? (
                    <div className="flex flex-col items-center justify-center py-20 text-muted-steel">
                        <Loader2 className="w-10 h-10 animate-spin mb-4 text-primary" />
                        <p className="text-lg">Analyzing your emails to find upcoming events...</p>
                    </div>
                ) : error ? (
                    <div className="flex flex-col items-center justify-center py-20 text-red-500">
                        <AlertCircle className="w-12 h-12 mb-4 opacity-50" />
                        <p className="text-lg font-medium">{error}</p>
                    </div>
                ) : events.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-20 text-muted-steel">
                        <p className="text-lg">No upcoming events found in your emails.</p>
                    </div>
                ) : (
                    <div className="overflow-x-auto border border-whisper rounded-xl shadow-sm">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="bg-canvas-gray border-b border-whisper">
                                    <th className="px-6 py-4 font-semibold text-charcoal-ink text-sm">Event Title</th>
                                    <th className="px-6 py-4 font-semibold text-charcoal-ink text-sm">Date & Time</th>
                                    <th className="px-6 py-4 font-semibold text-charcoal-ink text-sm">Location</th>
                                    <th className="px-6 py-4 font-semibold text-charcoal-ink text-sm">Description</th>
                                    <th className="px-6 py-4 font-semibold text-charcoal-ink text-sm">Status</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-whisper">
                                {events.map((event, index) => (
                                    <tr key={index} className="hover:bg-canvas-gray/50 transition-colors">
                                        <td className="px-6 py-4 text-charcoal-ink font-medium max-w-xs truncate" title={event.title}>{event.title}</td>
                                        <td className="px-6 py-4 text-muted-steel text-sm whitespace-pre-wrap">{event.datetime}</td>
                                        <td className="px-6 py-4 text-muted-steel text-sm max-w-xs truncate" title={event.location}>{event.location}</td>
                                        <td className="px-6 py-4 text-muted-steel text-sm max-w-sm truncate" title={event.description}>{event.description}</td>
                                        <td className="px-6 py-4">
                                            <span className={`px-2.5 py-1 text-xs font-semibold rounded-full ${
                                                event.status?.toUpperCase() === 'UPCOMING' ? 'bg-blue-50 text-blue-600' :
                                                event.status?.toUpperCase() === 'TENTATIVE' ? 'bg-yellow-50 text-yellow-600' :
                                                'bg-spring-green/10 text-spring-green'
                                            }`}>
                                                {event.status}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
                
                {rawAnalysis && (
                    <div className="mt-8 p-6 bg-blue-50/50 border border-blue-100 rounded-xl">
                        <h2 className="text-lg font-semibold text-charcoal-ink mb-3 flex items-center">
                            <span className="bg-blue-100 text-blue-600 p-1.5 rounded-lg mr-2">
                                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                            </span>
                            AI Analysis Summary
                        </h2>
                        <p className="text-muted-steel leading-relaxed whitespace-pre-wrap">{rawAnalysis}</p>
                    </div>
                )}
            </div>
        </div>
    );
}
