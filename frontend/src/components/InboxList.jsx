import React from 'react';
import { Search, Loader2, Paperclip, Inbox, Users, Tag, Info } from 'lucide-react';

export default function InboxList({ 
	folder = 'inbox', 
	selectedThreadId, 
	onSelectThread, 
	threads = [], 
	isLoading = false, 
	activeTab = 'primary', 
	onTabChange 
}) {
	const folderTitle = {
		inbox:  'Inbox',
		sent:   'Sent',
		drafts: 'Drafts',
		trash:  'Trash',
	}[folder] ?? 'Inbox';
	const tabs = [
		{ id: 'primary',    label: 'Primary',    icon: Inbox },
		{ id: 'promotions', label: 'Promotions', icon: Tag   },
		{ id: 'social',     label: 'Social',     icon: Users },
		{ id: 'updates',    label: 'Updates',    icon: Info  },
	];

	return (
		<div className="w-[380px] bg-pure-surface border-r border-whisper/50 flex flex-col h-screen">

			{/* Header & Search */}
			<div className="px-5 pt-6 pb-4 border-b border-whisper/50">
				<h2 className="text-xl text-charcoal-ink mb-4">{folderTitle}</h2>
				<div className="flex items-center bg-canvas-gray px-3 py-2 rounded-lg gap-2">
					<Search size={16} className="text-muted-steel shrink-0" />
					<input
						type="text"
						placeholder="Search mail or ask AI..."
						className="border-none bg-transparent outline-none w-full text-sm font-sans text-charcoal-ink placeholder:text-muted-steel"
					/>
				</div>
			</div>

			{/* Tabs — only shown for inbox */}
			{folder === 'inbox' && (
				<div className="flex border-b border-whisper/50 bg-canvas-gray">
					{tabs.map(tab => {
						const Icon = tab.icon;
						const isActive = activeTab === tab.id;
						return (
							<button
								key={tab.id}
								onClick={() => onTabChange && onTabChange(tab.id)}
								className={`flex-1 flex flex-col items-center py-3 bg-transparent cursor-pointer transition-all gap-1 border-b-2 ${
									isActive
										? 'text-emerald-accent border-emerald-accent font-semibold'
										: 'text-muted-steel border-transparent font-medium'
								}`}
							>
								<Icon size={18} />
								<span className="text-[11px]">{tab.label}</span>
							</button>
						);
					})}
				</div>
			)}

			{/* Thread List */}
			<div className="flex-1 overflow-y-auto">
				{isLoading ? (
					<div className="flex justify-center py-10 text-muted-steel">
						<Loader2 className="animate-spin" size={24} />
					</div>
				) : threads.length === 0 ? (
					<div className="text-center py-10 px-5 text-muted-steel text-sm">
						No threads found.
					</div>
				) : (
					threads.map((thread) => {
						const isSelected = thread.id === selectedThreadId;
						const senderName = thread.latestSenderName || 'Unknown';
						const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(senderName)}&background=random&color=fff&rounded=true&bold=true`;

						return (
							<div
								key={thread.id}
								onClick={() => onSelectThread(thread.id)}
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
									<img src={avatarUrl} alt={senderName} className="w-full h-full rounded-full" />
								</div>

								{/* Content */}
								<div className="flex-1 min-w-0">
									<div className="flex justify-between items-baseline mb-1">
										<div className="flex items-center gap-2 truncate">
											<span className={`text-charcoal-ink text-sm truncate ${thread.unread ? 'font-bold' : 'font-medium'}`}>
												{senderName}
											</span>
											{thread.unread && (
												<span className="shrink-0 bg-emerald-accent/10 text-emerald-accent text-[10px] font-bold px-1.5 py-0.5 rounded uppercase tracking-wider">
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
												{thread.latestDate ? new Date(thread.latestDate).toLocaleDateString() : ''}
											</span>
										</div>
									</div>
									<div className={`text-sm text-charcoal-ink mb-1 truncate ${thread.unread ? 'font-semibold' : 'font-medium'}`}>
										{thread.subject}
									</div>
									<div className="text-[13px] text-muted-steel line-clamp-2 leading-[1.4]">
										{thread.snippet}
									</div>
								</div>
							</div>
						);
					})
				)}
			</div>
		</div>
	);
}
