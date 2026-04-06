import React, { useState } from 'react';
import { X, Maximize2, Minus, Paperclip, Link, Smile, Image as ImageIcon, Sparkles, Send } from 'lucide-react';

export default function ComposeModal({ onClose }) {
	const [recipient, setRecipient] = useState('');
	const [subject, setSubject] = useState('');
	const [body, setBody] = useState('');

	return (
		<div className="fixed bottom-6 right-20 w-[500px] h-[500px] bg-pure-surface border border-whisper/50 rounded-xl shadow-[0_8px_30px_rgba(0,0,0,0.12)] flex flex-col z-[1000] overflow-hidden">

			{/* Header */}
			<div className="bg-canvas-gray px-4 py-3 flex justify-between items-center border-b border-whisper/50">
				<div className="font-semibold text-sm text-charcoal-ink">New Message</div>
				<div className="flex gap-3 text-muted-steel">
					<button className="text-inherit flex items-center hover:text-charcoal-ink transition-colors"><Minus size={16} /></button>
					<button className="text-inherit flex items-center hover:text-charcoal-ink transition-colors"><Maximize2 size={16} /></button>
					<button className="text-inherit flex items-center hover:text-charcoal-ink transition-colors" onClick={onClose}><X size={16} /></button>
				</div>
			</div>

			{/* Form Fields */}
			<div className="px-4">
				<input
					type="text"
					placeholder="To"
					value={recipient}
					onChange={(e) => setRecipient(e.target.value)}
					className="w-full py-3 border-none border-b border-whisper/50 outline-none text-sm font-sans text-charcoal-ink placeholder:text-muted-steel bg-transparent"
					style={{ borderBottom: '1px solid rgba(226, 232, 240, 0.5)' }}
				/>
				<input
					type="text"
					placeholder="Subject"
					value={subject}
					onChange={(e) => setSubject(e.target.value)}
					className="w-full py-3 border-none outline-none text-sm font-sans text-charcoal-ink font-semibold placeholder:text-muted-steel bg-transparent"
					style={{ borderBottom: '1px solid rgba(226, 232, 240, 0.5)' }}
				/>
			</div>

			{/* Body Area */}
			<div className="flex-1 p-4 relative">
				<textarea
					value={body}
					onChange={(e) => setBody(e.target.value)}
					className="w-full h-full border-none outline-none resize-none text-[15px] leading-relaxed font-sans text-charcoal-ink bg-transparent"
				/>
			</div>

			{/* Footer / Toolbar */}
			<div className="px-4 py-3 border-t border-whisper/50 flex items-center justify-between bg-pure-surface">
				<div className="flex items-center gap-4">
					<button className="flex items-center gap-2 bg-emerald-accent text-white font-semibold px-5 py-2 rounded-full hover:bg-emerald-hover transition-colors">
						<Send size={14} className="mr-1" />
						Send
					</button>
					<div className="flex gap-3 text-muted-steel">
						<button className="text-inherit flex items-center hover:text-charcoal-ink transition-colors"><Paperclip size={18} /></button>
						<button className="text-inherit flex items-center hover:text-charcoal-ink transition-colors"><Link size={18} /></button>
						<button className="text-inherit flex items-center hover:text-charcoal-ink transition-colors"><Smile size={18} /></button>
						<button className="text-inherit flex items-center hover:text-charcoal-ink transition-colors"><ImageIcon size={18} /></button>
					</div>
				</div>
				<div>
					<button className="flex items-center gap-2 text-emerald-accent font-semibold text-[13px] px-3 py-1.5 bg-canvas-gray rounded-full border-none cursor-pointer hover:opacity-80 transition-opacity">
						<Sparkles size={14} />
						Write with AI
					</button>
				</div>
			</div>
		</div>
	);
}
