import React from 'react';
import { Search, Loader2, Paperclip, Inbox, Users, Tag, Info } from 'lucide-react';

export default function InboxList({ selectedEmailId, onSelectEmail, emails = [], isLoading = false, activeTab = 'primary', onTabChange }) {
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
				<h2 className="text-xl text-charcoal-ink mb-4">Inbox</h2>
				<div className="flex items-center bg-canvas-gray px-3 py-2 rounded-lg gap-2">
					<Search size={16} className="text-muted-steel shrink-0" />
					<input
						type="text"
						placeholder="Search mail or ask AI..."
						className="border-none bg-transparent outline-none w-full text-sm font-sans text-charcoal-ink placeholder:text-muted-steel"
					/>
				</div>
			</div>

			{/* Tabs */}
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

			{/* Email List */}
			<div className="flex-1 overflow-y-auto">
				{isLoading ? (
					<div className="flex justify-center py-10 text-muted-steel">
						<Loader2 className="animate-spin" size={24} />
					</div>
				) : emails.length === 0 ? (
					<div className="text-center py-10 px-5 text-muted-steel text-sm">
						No emails found.
					</div>
				) : (
					emails.map((email) => {
						const isSelected = email.id === selectedEmailId;
						const senderName = email.senderName || 'Unknown';
						const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(senderName)}&background=random&color=fff&rounded=true&bold=true`;

						return (
							<div
								key={email.id}
								onClick={() => onSelectEmail(email.id)}
								className={`px-5 py-4 border-b border-whisper/50 cursor-pointer transition-colors flex gap-3 ${
									isSelected
										? 'bg-canvas-gray'
										: email.unread
										? 'bg-[#f9fafa]'
										: 'bg-transparent hover:bg-canvas-gray/60'
								}`}
							>
								{/* Avatar */}
								<div className="relative w-10 h-10 shrink-0">
									<img src={avatarUrl} alt={senderName} className="w-full h-full rounded-full" />
									{email.unread && (
										<div className="absolute -top-0.5 -right-0.5 w-2.5 h-2.5 bg-emerald-accent rounded-full border-2 border-white" />
									)}
								</div>

								{/* Content */}
								<div className="flex-1 min-w-0">
									<div className="flex justify-between items-baseline mb-1">
										<span className={`text-charcoal-ink text-sm truncate ${email.unread ? 'font-bold' : 'font-medium'}`}>
											{senderName}
										</span>
										<div className="flex gap-1.5 items-center shrink-0 ml-2">
											{email.attachments && email.attachments.length > 0 && (
												<Paperclip size={14} className="text-muted-steel" />
											)}
											<span className={`text-xs whitespace-nowrap ${
												email.unread
													? 'text-charcoal-ink font-semibold'
													: 'text-muted-steel font-normal'
											}`}>
												{email.date ? new Date(email.date).toLocaleDateString() : ''}
											</span>
										</div>
									</div>
									<div className={`text-sm text-charcoal-ink mb-1 truncate ${email.unread ? 'font-semibold' : 'font-medium'}`}>
										{email.subject}
									</div>
									<div className="text-[13px] text-muted-steel line-clamp-2 leading-[1.4]">
										{email.snippet}
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
