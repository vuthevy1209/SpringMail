import React from 'react';

const mockEvents = [
    {
        id: 1,
        title: "Team Sync Meeting",
        date: "2024-05-15",
        time: "10:00 AM",
        location: "Conference Room A",
        status: "Upcoming",
    },
    {
        id: 2,
        title: "Q2 Planning Session",
        date: "2024-05-18",
        time: "02:00 PM",
        location: "Virtual (Zoom)",
        status: "Upcoming",
    },
    {
        id: 3,
        title: "Product Launch",
        date: "2024-05-22",
        time: "09:00 AM",
        location: "Main Auditorium",
        status: "Upcoming",
    },
    {
        id: 4,
        title: "Client Check-in",
        date: "2024-05-25",
        time: "11:30 AM",
        location: "Virtual (Google Meet)",
        status: "Tentative",
    },
    {
        id: 5,
        title: "Company Picnic",
        date: "2024-06-01",
        time: "12:00 PM",
        location: "Central Park",
        status: "Scheduled",
    },
];

export default function EventPage() {
    return (
        <div className="flex-1 bg-white h-full overflow-y-auto">
            <div className="p-8">
                <div className="flex items-center justify-between mb-8">
                    <h1 className="text-2xl font-bold text-charcoal-ink">Upcoming Events</h1>
                </div>
                
                <div className="overflow-x-auto border border-whisper rounded-xl shadow-sm">
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-canvas-gray border-b border-whisper">
                                <th className="px-6 py-4 font-semibold text-charcoal-ink text-sm">Event Title</th>
                                <th className="px-6 py-4 font-semibold text-charcoal-ink text-sm">Date</th>
                                <th className="px-6 py-4 font-semibold text-charcoal-ink text-sm">Time</th>
                                <th className="px-6 py-4 font-semibold text-charcoal-ink text-sm">Location</th>
                                <th className="px-6 py-4 font-semibold text-charcoal-ink text-sm">Status</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-whisper">
                            {mockEvents.map((event) => (
                                <tr key={event.id} className="hover:bg-canvas-gray/50 transition-colors">
                                    <td className="px-6 py-4 text-charcoal-ink font-medium">{event.title}</td>
                                    <td className="px-6 py-4 text-muted-steel text-sm">{event.date}</td>
                                    <td className="px-6 py-4 text-muted-steel text-sm">{event.time}</td>
                                    <td className="px-6 py-4 text-muted-steel text-sm">{event.location}</td>
                                    <td className="px-6 py-4">
                                        <span className={`px-2.5 py-1 text-xs font-semibold rounded-full ${
                                            event.status === 'Upcoming' ? 'bg-blue-50 text-blue-600' :
                                            event.status === 'Tentative' ? 'bg-yellow-50 text-yellow-600' :
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
            </div>
        </div>
    );
}
