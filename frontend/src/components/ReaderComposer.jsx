import React, { useState } from 'react';
import { Sparkles, Reply, ListChecks, Send, X, Paperclip, FileText } from 'lucide-react';

export default function ReaderComposer({ selectedEmailId, emails = [] }) {
	const [composerOpen, setComposerOpen] = useState(false);
	const [isGenerating, setIsGenerating] = useState(false);
	const [draftContent, setDraftContent] = useState('');

	const email = emails.find(e => e.id === selectedEmailId);

	if (!email) {
		return (
			<div className="flex-1 flex items-center justify-center bg-canvas-gray">
				<div className="text-center text-muted-steel">
					<Sparkles size={32} className="mx-auto mb-4 opacity-50" />
					<p>Select an email to read</p>
				</div>
			</div>
		);
	}

	const handleDraftReply = () => {
		setComposerOpen(true);
		setIsGenerating(true);
		setTimeout(() => {
			setIsGenerating(false);
			setDraftContent("Thank you for sending this over.\n\nI will review the attached documents with the team and get back to you by early next week. Let's touch base again on Wednesday.\n\nBest,\nSpringMail User");
		}, 2000);
	};

	return (
		<div className="flex-1 flex flex-col h-screen bg-canvas-gray overflow-hidden">

			{/* LLM Actions Toolbar */}
			<div className="px-6 py-4 border-b border-whisper/50 flex gap-3 bg-pure-surface">
				<button className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors">
					<Sparkles size={16} className="text-emerald-accent" />
					Summarize
				</button>
				<button className="flex items-center gap-2 border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-pure-surface font-medium hover:bg-canvas-gray transition-colors">
					<ListChecks size={16} />
					Extract Action Items
				</button>
				<div className="flex-1" />
				<button
					onClick={handleDraftReply}
					className="flex items-center gap-2 bg-emerald-accent text-white px-4 py-2 rounded-lg font-semibold hover:bg-emerald-hover transition-colors"
				>
					<Reply size={16} />
					Draft Reply
				</button>
			</div>

			{/* Reading Pane */}
			<div className="flex-1 px-12 py-8 overflow-y-auto">
				<div className="max-w-[800px] mx-auto">
					<h1 className="text-2xl text-charcoal-ink mb-6">
						{email.subject}
					</h1>

					{/* Sender Info */}
					<div className="flex justify-between items-center mb-8">
						<div className="flex items-center gap-3">
							<div className="w-10 h-10 shrink-0">
								<img
									src={`https://ui-avatars.com/api/?name=${encodeURIComponent(email.senderName || 'Unknown')}&background=random&color=fff&rounded=true&bold=true`}
									alt={email.senderName}
									className="w-full h-full rounded-full"
								/>
							</div>
							<div>
								<div className="font-semibold text-charcoal-ink">
									{email.senderName || (email.from && email.from.includes('<') ? email.from.split('<')[0].trim() : email.from)}
								</div>
								<div className="text-[13px] text-muted-steel">
									{email.from && email.from.includes('<') ? '<' + email.from.split('<')[1] : ''}
								</div>
							</div>
						</div>
						<div className="text-muted-steel text-sm">
							{email.date ? new Date(email.date).toLocaleString() : ''}
						</div>
					</div>

					{/* Email Body */}
					<div
						className="text-charcoal-ink text-[15px] leading-relaxed break-words"
						dangerouslySetInnerHTML={{ __html: email.content }}
					/>

					{/* Attachments */}
					{email.attachments && email.attachments.length > 0 && (
						<div className="mt-8 pt-6 border-t border-whisper/50">
							<div className="font-semibold text-charcoal-ink mb-4 flex items-center gap-2">
								<Paperclip size={16} />
								{email.attachments.length} Attachments
							</div>
							<div className="flex flex-wrap gap-3">
								{email.attachments.map((att, idx) => (
									<div
										key={idx}
										className="flex items-center gap-2 px-3 py-2 border border-whisper/50 rounded-lg bg-pure-surface text-[13px] text-charcoal-ink shadow-[0_1px_2px_rgba(0,0,0,0.05)]"
									>
										<FileText size={16} className="text-emerald-accent" />
										<span className="max-w-[200px] truncate" title={att}>{att}</span>
									</div>
								))}
							</div>
						</div>
					)}
				</div>
			</div>

			{/* LLM Composer Pane */}
			{composerOpen && (
				<div className="h-[40vh] border-t border-whisper/50 bg-pure-surface px-8 py-6 flex flex-col shadow-[0_-4px_20px_rgba(0,0,0,0.03)]">

					{/* Composer Header */}
					<div className="flex justify-between items-center mb-4">
						<div className="flex items-center gap-2 text-emerald-accent font-semibold text-sm">
							<Sparkles size={16} />
							LLM Draft
						</div>
						<button onClick={() => setComposerOpen(false)} className="text-muted-steel">
							<X size={20} />
						</button>
					</div>

					{isGenerating ? (
						<div className="flex-1 flex flex-col gap-3">
							<div className="h-3 w-[90%] bg-canvas-gray rounded animate-pulse" />
							<div className="h-3 w-full bg-canvas-gray rounded animate-pulse [animation-delay:0.2s]" />
							<div className="h-3 w-[80%] bg-canvas-gray rounded animate-pulse [animation-delay:0.4s]" />
						</div>
					) : (
						<>
							<textarea
								value={draftContent}
								onChange={(e) => setDraftContent(e.target.value)}
								className="flex-1 border-none outline-none resize-none font-sans text-[15px] leading-relaxed text-charcoal-ink mb-4 bg-transparent"
							/>

							<div className="flex justify-between items-center pt-3 border-t border-whisper/50">
								<div className="flex items-center gap-4">
									<button className="flex items-center gap-2 bg-emerald-accent text-white font-semibold px-5 py-2 rounded-full hover:bg-emerald-hover transition-colors">
										<Send size={14} className="mr-1" />
										Send
									</button>
									<div className="flex gap-3 text-muted-steel">
										<button className="text-inherit bg-transparent border-none p-0 cursor-pointer">
											<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m21.44 11.05-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48" /></svg>
										</button>
										<button className="text-inherit bg-transparent border-none p-0 cursor-pointer">
											<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" /><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" /></svg>
										</button>
										<button className="text-inherit bg-transparent border-none p-0 cursor-pointer">
											<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10" /><path d="M8 14s1.5 2 4 2 4-2 4-2" /><line x1="9" x2="9.01" y1="9" y2="9" /><line x1="15" x2="15.01" y1="9" y2="9" /></svg>
										</button>
										<button className="text-inherit bg-transparent border-none p-0 cursor-pointer">
											<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" ry="2" /><circle cx="9" cy="9" r="2" /><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21" /></svg>
										</button>
									</div>
								</div>
								<div className="flex gap-3">
									<button
										className="border border-whisper/50 text-charcoal-ink px-4 py-2 rounded-lg bg-transparent font-medium hover:bg-canvas-gray transition-colors"
										onClick={() => setComposerOpen(false)}
									>
										Discard
									</button>
									<button className="flex items-center gap-2 text-emerald-accent font-semibold text-[13px] px-3 py-1.5 bg-canvas-gray rounded-full">
										<Sparkles size={14} />
										Refine with AI
									</button>
								</div>
							</div>
						</>
					)}
				</div>
			)}
		</div>
	);
}